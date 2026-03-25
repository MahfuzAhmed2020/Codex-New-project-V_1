package com.example.demoapi.model;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String address,
        String zip,
        String phone,
        String email) {
}
