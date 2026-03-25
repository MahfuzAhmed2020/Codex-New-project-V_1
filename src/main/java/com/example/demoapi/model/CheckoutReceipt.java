package com.example.demoapi.model;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutReceipt(
        Long orderId,
        Long userId,
        String email,
        String shippingAddress,
        String maskedCard,
        String trackingNumber,
        String status,
        List<CartItem> items,
        BigDecimal totalAmount,
        boolean emailSent,
        String orderTimeEst) {
}
