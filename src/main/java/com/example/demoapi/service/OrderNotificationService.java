package com.example.demoapi.service;

import com.example.demoapi.model.CheckoutOrder;
import com.example.demoapi.model.User;

public interface OrderNotificationService {

    boolean sendTrackingEmail(User user, CheckoutOrder order);
}
