package com.example.demoapi.model;

public record UserProfile(
        Long id,
        String firstName,
        String lastName,
        String address,
        String zip,
        String phone,
        String email) {
}
