package com.example.demoapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.ApiMessage;
import com.example.demoapi.model.ConfirmEmailRequest;
import com.example.demoapi.model.ForgotPasswordRequest;
import com.example.demoapi.model.LoginRequest;
import com.example.demoapi.model.LoginResponse;
import com.example.demoapi.model.RegisterUserRequest;
import com.example.demoapi.model.ResetPasswordRequest;
import com.example.demoapi.model.UpdateProfileRequest;
import com.example.demoapi.model.User;
import com.example.demoapi.model.UserProfile;

@Service
public class UserService {

    private final AtomicLong nextUserId = new AtomicLong(1);
    private final List<User> users = new ArrayList<>();
    private final Map<String, String> confirmationTokens = new HashMap<>();
    private final Map<String, String> resetTokens = new HashMap<>();
    private final AccountNotificationService accountNotificationService;

    public UserService(AccountNotificationService accountNotificationService) {
        this.accountNotificationService = accountNotificationService;
    }

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
                request.password(),
                false);

        users.add(user);
        String token = createToken();
        confirmationTokens.put(token, user.email());
        accountNotificationService.sendRegistrationConfirmationEmail(user, token);
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

    public ApiMessage forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        User user = findUserByEmailOrNull(normalizeEmail(request.email()));
        if (user != null) {
            String token = createToken();
            resetTokens.put(token, user.email());
            accountNotificationService.sendPasswordResetEmail(user, token);
        }

        return new ApiMessage("If the email exists, a password reset link was sent.");
    }

    public ApiMessage resetPassword(ResetPasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.token() == null || request.token().isBlank()) {
            throw new IllegalArgumentException("Reset token is required.");
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

        String email = resetTokens.remove(request.token());
        if (email == null) {
            throw new IllegalArgumentException("Reset token is invalid or expired.");
        }

        User existingUser = findUserByEmail(email);
        User updatedUser = new User(
                existingUser.id(),
                existingUser.firstName(),
                existingUser.lastName(),
                existingUser.address(),
                existingUser.zip(),
                existingUser.phone(),
                existingUser.email(),
                request.password(),
                existingUser.emailConfirmed());
        replaceUser(existingUser, updatedUser);

        return new ApiMessage("Password reset successfully.");
    }

    public ApiMessage confirmEmail(ConfirmEmailRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new IllegalArgumentException("Confirmation token is required.");
        }

        String email = confirmationTokens.remove(request.token());
        if (email == null) {
            throw new IllegalArgumentException("Confirmation token is invalid or expired.");
        }

        User existingUser = findUserByEmail(email);
        User updatedUser = new User(
                existingUser.id(),
                existingUser.firstName(),
                existingUser.lastName(),
                existingUser.address(),
                existingUser.zip(),
                existingUser.phone(),
                existingUser.email(),
                existingUser.password(),
                true);
        replaceUser(existingUser, updatedUser);

        return new ApiMessage("Email confirmed successfully.");
    }

    public UserProfile getUserProfile(Long userId) {
        return toProfile(findUserById(userId));
    }

    public UserProfile updateProfile(Long userId, UpdateProfileRequest request) {
        validateProfileUpdateRequest(request);

        User existingUser = findUserById(userId);
        String normalizedEmail = normalizeEmail(request.email());
        User sameEmailUser = findUserByEmailOrNull(normalizedEmail);
        if (sameEmailUser != null && !sameEmailUser.id().equals(userId)) {
            throw new IllegalArgumentException("A user with email " + normalizedEmail + " already exists.");
        }

        boolean emailChanged = !existingUser.email().equals(normalizedEmail);
        User updatedUser = new User(
                existingUser.id(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.address().trim(),
                request.zip().trim(),
                request.phone().trim(),
                normalizedEmail,
                existingUser.password(),
                emailChanged ? false : existingUser.emailConfirmed());

        replaceUser(existingUser, updatedUser);

        if (emailChanged) {
            String token = createToken();
            confirmationTokens.put(token, updatedUser.email());
            accountNotificationService.sendRegistrationConfirmationEmail(updatedUser, token);
        }

        return toProfile(updatedUser);
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
        confirmationTokens.clear();
        resetTokens.clear();
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

    private void validateProfileUpdateRequest(UpdateProfileRequest request) {
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

    private void replaceUser(User existingUser, User updatedUser) {
        int index = users.indexOf(existingUser);
        users.set(index, updatedUser);
    }

    private String createToken() {
        return UUID.randomUUID().toString();
    }

    private UserProfile toProfile(User user) {
        return new UserProfile(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.address(),
                user.zip(),
                user.phone(),
                user.email(),
                user.emailConfirmed());
    }
}
