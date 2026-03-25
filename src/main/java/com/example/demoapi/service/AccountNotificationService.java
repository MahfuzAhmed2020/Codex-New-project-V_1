package com.example.demoapi.service;

import com.example.demoapi.model.User;

public interface AccountNotificationService {

    boolean sendRegistrationConfirmationEmail(User user, String token);

    boolean sendPasswordResetEmail(User user, String token);
}
