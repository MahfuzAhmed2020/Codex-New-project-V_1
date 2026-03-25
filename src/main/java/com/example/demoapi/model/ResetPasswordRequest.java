package com.example.demoapi.model;

public record ResetPasswordRequest(String token, String password, String confirmPassword) {
}
