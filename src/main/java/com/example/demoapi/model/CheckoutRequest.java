package com.example.demoapi.model;

public record CheckoutRequest(String shippingAddress, String cardNumber) {
}
