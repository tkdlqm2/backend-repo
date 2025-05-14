package com.example.orderservice.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private String orderNumber;
    private BigDecimal amount;
    private String paymentMethod;

    // 생성자, 게터, 세터
    public PaymentRequest() {
    }

    public PaymentRequest(String orderNumber, BigDecimal amount, String paymentMethod) {
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}