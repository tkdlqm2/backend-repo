package com.example.orderservice.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    SHIPPING,
    COMPLETED,
    CANCELLED
}