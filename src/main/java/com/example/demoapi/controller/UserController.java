package com.example.demoapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demoapi.model.CheckoutOrder;
import com.example.demoapi.model.UpdateProfileRequest;
import com.example.demoapi.model.UserProfile;
import com.example.demoapi.service.CheckoutService;
import com.example.demoapi.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CheckoutService checkoutService;

    public UserController(UserService userService, CheckoutService checkoutService) {
        this.userService = userService;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public List<UserProfile> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserProfile getUser(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public UserProfile updateProfile(@PathVariable Long userId, @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/{userId}/orders")
    public List<CheckoutOrder> getUserOrders(@PathVariable Long userId) {
        return checkoutService.getOrdersForUser(userId);
    }
}
