package com.example.demoapi.model;

import java.math.BigDecimal;

public record CartItem(
        Long menuItemId,
        String name,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal) {
}
