package com.example.demoapi.model;

import java.util.List;

public record CheckoutWhitelist(List<String> addresses, List<String> cards) {
}
