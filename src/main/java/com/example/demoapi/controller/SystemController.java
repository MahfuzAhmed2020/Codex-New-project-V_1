package com.example.demoapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/docs/urls")
    public Map<String, Object> urls() {
        return Map.of(
                "frontend", List.of(
                        "/index.html",
                        "/login.html",
                        "/forgot-password.html",
                        "/reset-password.html?token=<token>",
                        "/account-confirmed.html?token=<token>",
                        "/profile.html",
                        "/menu.html",
                        "/checkout.html",
                        "/confirmation.html",
                        "/order-history.html",
                        "/order-status.html?tracking=<trackingNumber>"),
                "auth", List.of(
                        "POST /api/auth/register",
                        "POST /api/auth/login",
                        "POST /api/auth/forgot-password",
                        "POST /api/auth/reset-password",
                        "POST /api/auth/confirm-email",
                        "POST /api/auth/logout"),
                "users", List.of(
                        "GET /api/users",
                        "GET /api/users/{userId}",
                        "PUT /api/users/{userId}/profile",
                        "GET /api/users/{userId}/orders"),
                "menu", List.of(
                        "GET /api/menu",
                        "GET /api/menu/{id}",
                        "POST /api/menu",
                        "PATCH /api/menu/{id}",
                        "DELETE /api/menu/{id}"),
                "cart", List.of(
                        "GET /api/users/{userId}/cart",
                        "POST /api/users/{userId}/cart/items",
                        "PATCH /api/users/{userId}/cart/items/{menuItemId}",
                        "DELETE /api/users/{userId}/cart/items/{menuItemId}"),
                "checkout", List.of(
                        "GET /api/checkout/whitelist",
                        "POST /api/users/{userId}/checkout",
                        "GET /api/tracking/{trackingNumber}"),
                "legacyOrders", List.of(
                        "GET /api/orders",
                        "GET /api/orders/{id}",
                        "POST /api/orders",
                        "PATCH /api/orders/{id}",
                        "PUT /api/orders/{id}",
                        "DELETE /api/orders/{id}"));
    }
}

