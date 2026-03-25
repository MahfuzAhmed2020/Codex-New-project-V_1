package com.example.demoapi.model;

import java.math.BigDecimal;
import java.util.List;

public record Cart(Long userId, List<CartItem> items, BigDecimal totalAmount) {
}
