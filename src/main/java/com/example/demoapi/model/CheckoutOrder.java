package com.example.demoapi.model;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutOrder(
        Long id,
        Long userId,
        String customerName,
        String email,
        String shippingAddress,
        String phoneNumber,
        List<CartItem> items,
        BigDecimal totalAmount,
        String trackingNumber,
        String status) {
}
