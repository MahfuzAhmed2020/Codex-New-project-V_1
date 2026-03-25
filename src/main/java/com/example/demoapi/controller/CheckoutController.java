package com.example.demoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demoapi.model.CheckoutReceipt;
import com.example.demoapi.model.CheckoutRequest;
import com.example.demoapi.model.CheckoutWhitelist;
import com.example.demoapi.model.TrackingDetails;
import com.example.demoapi.service.CheckoutService;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout/whitelist")
    public CheckoutWhitelist getWhitelist() {
        return checkoutService.getWhitelist();
    }

    @PostMapping("/users/{userId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutReceipt checkout(@PathVariable Long userId, @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(userId, request);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public TrackingDetails getTracking(@PathVariable String trackingNumber) {
        return checkoutService.getTracking(trackingNumber);
    }
}
