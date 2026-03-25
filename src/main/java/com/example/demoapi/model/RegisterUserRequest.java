package com.example.demoapi.model;

public record RegisterUserRequest(
        String firstName,
        String lastName,
        String address,
        String zip,
        String phone,
        String email,
        String password,
        String confirmPassword) {
}
