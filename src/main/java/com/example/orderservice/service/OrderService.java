package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.PaymentRequest;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient paymentWebClient;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        WebClient paymentWebClient) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentWebClient = paymentWebClient;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // 주문 생성
        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());

        // 주문 아이템 추가
        orderRequest.getItems().forEach(itemRequest -> {
            OrderItem item = new OrderItem(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    BigDecimal.valueOf(itemRequest.getPrice())
            );
            order.addItem(item);
        });

        // 총액 계산
        order.calculateTotalAmount();

        // 저장
        Order savedOrder = orderRepository.save(order);

        // 이벤트 발행
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerEmail(),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );
        kafkaTemplate.send("order-created-topic", event);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse requestPayment(String orderNumber, String paymentMethod) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        Order updatedOrder = orderRepository.save(order);

        // 결제 요청 생성
        PaymentRequest paymentRequest = new PaymentRequest(
                order.getOrderNumber(),
                order.getTotalAmount(),
                paymentMethod
        );

        // 결제 서비스에 비동기 요청
        paymentWebClient.post()
                .uri("/api/payments")
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderNumber, String paymentId, OrderStatus status) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        order.setStatus(status);
        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaymentId(order.getPaymentId());

        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setProductName(item.getProductName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);

        return response;
    }
}