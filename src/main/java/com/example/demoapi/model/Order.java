package com.example.demoapi.model;

import java.math.BigDecimal;
import java.util.List;

public record Order(
        Long id,
        String customerName,
        String address,
        String phoneNumber,
        List<MenuItem> items,
        BigDecimal totalAmount) {
}
