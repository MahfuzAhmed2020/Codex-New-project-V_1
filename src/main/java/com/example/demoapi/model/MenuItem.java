package com.example.demoapi.model;

import java.math.BigDecimal;

public record MenuItem(Long id, String name, String description, BigDecimal price) {
}
