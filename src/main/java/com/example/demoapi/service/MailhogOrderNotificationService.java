package com.example.demoapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demoapi.model.CheckoutOrder;
import com.example.demoapi.model.User;

@Service
public class MailhogOrderNotificationService implements OrderNotificationService {

    private final JavaMailSender mailSender;
    private final String appBaseUrl;

    public MailhogOrderNotificationService(
            JavaMailSender mailSender,
            @Value("${app.base-url}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public boolean sendTrackingEmail(User user, CheckoutOrder order) {
        String statusUrl = appBaseUrl + "/order-status.html?tracking=" + order.trackingNumber();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@demo-api.local");
        message.setTo(user.email());
        message.setSubject("Your order tracking number");
        message.setText("""
                Hi %s,

                Thanks for your order. Your tracking number is %s.
                Order status: %s
                Shipping address: %s

                View your order status in the browser:
                %s
                """.formatted(
                user.firstName(),
                order.trackingNumber(),
                order.status(),
                order.shippingAddress(),
                statusUrl));

        try {
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            return false;
        }
    }
}
