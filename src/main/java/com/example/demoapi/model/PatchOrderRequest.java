package com.example.demoapi.model;

import java.util.List;

public record PatchOrderRequest(String customerName, String address, String phoneNumber, List<Long> itemIds) {
}
