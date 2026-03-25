package com.example.demoapi.model;

public record User(
        Long id,
        String firstName,
        String lastName,
        String address,
        String zip,
        String phone,
        String email,
        String password) {
}
