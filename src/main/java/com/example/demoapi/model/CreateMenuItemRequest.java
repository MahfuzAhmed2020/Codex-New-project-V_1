package com.example.demoapi.model;

import java.math.BigDecimal;

public record CreateMenuItemRequest(String name, String description, BigDecimal price) {
}
