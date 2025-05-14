package com.example.orderservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderCreatedEvent {
    private String orderNumber;
    private String customerEmail;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    // 생성자, 게터, 세터
    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderNumber, String customerEmail, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}