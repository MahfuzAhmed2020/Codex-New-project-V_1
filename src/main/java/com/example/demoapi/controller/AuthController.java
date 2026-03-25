package com.example.demoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demoapi.model.ApiMessage;
import com.example.demoapi.model.ConfirmEmailRequest;
import com.example.demoapi.model.ForgotPasswordRequest;
import com.example.demoapi.model.LoginRequest;
import com.example.demoapi.model.LoginResponse;
import com.example.demoapi.model.RegisterUserRequest;
import com.example.demoapi.model.ResetPasswordRequest;
import com.example.demoapi.model.UserProfile;
import com.example.demoapi.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile register(@RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/forgot-password")
    public ApiMessage forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiMessage resetPassword(@RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }

    @PostMapping("/confirm-email")
    public ApiMessage confirmEmail(@RequestBody ConfirmEmailRequest request) {
        return userService.confirmEmail(request);
    }

    @PostMapping("/logout")
    public ApiMessage logout() {
        return new ApiMessage("Logged out successfully.");
    }
}

