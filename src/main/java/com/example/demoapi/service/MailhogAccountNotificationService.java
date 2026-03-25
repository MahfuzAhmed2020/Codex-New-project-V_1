package com.example.demoapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demoapi.model.User;

@Service
public class MailhogAccountNotificationService implements AccountNotificationService {

    private final JavaMailSender mailSender;
    private final String appBaseUrl;

    public MailhogAccountNotificationService(
            JavaMailSender mailSender,
            @Value("${app.base-url}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public boolean sendRegistrationConfirmationEmail(User user, String token) {
        String confirmationUrl = appBaseUrl + "/account-confirmed.html?token=" + token;
        return sendEmail(
                user.email(),
                "Confirm your account",
                """
                        Hi %s,

                        Welcome. Please confirm your account by opening this link in the browser:
                        %s
                        """.formatted(user.firstName(), confirmationUrl));
    }

    @Override
    public boolean sendPasswordResetEmail(User user, String token) {
        String resetUrl = appBaseUrl + "/reset-password.html?token=" + token;
        return sendEmail(
                user.email(),
                "Reset your password",
                """
                        Hi %s,

                        We received a forgot-password request. Reset your password here:
                        %s
                        """.formatted(user.firstName(), resetUrl));
    }

    private boolean sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@demo-api.local");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            return false;
        }
    }
}
