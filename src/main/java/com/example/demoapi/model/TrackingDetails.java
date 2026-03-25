package com.example.demoapi.model;

public record TrackingDetails(Long orderId, String trackingNumber, String status, String email) {
}
