package com.example.demoapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.LoginRequest;
import com.example.demoapi.model.LoginResponse;
import com.example.demoapi.model.RegisterUserRequest;
import com.example.demoapi.model.User;
import com.example.demoapi.model.UserProfile;

@Service
public class UserService {

    private final AtomicLong nextUserId = new AtomicLong(1);
    private final List<User> users = new ArrayList<>();

    public UserProfile register(RegisterUserRequest request) {
        validateRegistrationRequest(request);

        String normalizedEmail = normalizeEmail(request.email());
        if (findUserByEmailOrNull(normalizedEmail) != null) {
            throw new IllegalArgumentException("A user with email " + normalizedEmail + " already exists.");
        }

        User user = new User(
                nextUserId.getAndIncrement(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.address().trim(),
                request.zip().trim(),
                request.phone().trim(),
                normalizedEmail,
                request.password());

        users.add(user);
        return toProfile(user);
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        String normalizedEmail = normalizeEmail(request.email());
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        User user = findUserByEmail(normalizedEmail);
        if (!user.password().equals(request.password())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return new LoginResponse(user.id(), user.firstName(), user.lastName(), user.email());
    }

    public UserProfile getUserProfile(Long userId) {
        return toProfile(findUserById(userId));
    }

    public List<UserProfile> getUsers() {
        return users.stream()
                .sorted(Comparator.comparing(User::id))
                .map(this::toProfile)
                .toList();
    }

    public User findUserById(Long userId) {
        return users.stream()
                .filter(user -> user.id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " was not found."));
    }

    public void reset() {
        users.clear();
        nextUserId.set(1);
    }

    private void validateRegistrationRequest(RegisterUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("Address is required.");
        }
        if (request.zip() == null || request.zip().isBlank()) {
            throw new IllegalArgumentException("Zip is required.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("Phone is required.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Email must be valid.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (request.confirmPassword() == null || request.confirmPassword().isBlank()) {
            throw new IllegalArgumentException("Confirm password is required.");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password must match.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return email.trim().toLowerCase(Locale.US);
    }

    private User findUserByEmail(String normalizedEmail) {
        User user = findUserByEmailOrNull(normalizedEmail);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    private User findUserByEmailOrNull(String normalizedEmail) {
        return users.stream()
                .filter(user -> user.email().equals(normalizedEmail))
                .findFirst()
                .orElse(null);
    }

    private UserProfile toProfile(User user) {
        return new UserProfile(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.address(),
                user.zip(),
                user.phone(),
                user.email());
    }
}
