package com.example.demoapi.model;

import java.math.BigDecimal;

public record UpdateMenuItemRequest(String name, String description, BigDecimal price) {
}
