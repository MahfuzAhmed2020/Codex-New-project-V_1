package com.example.demoapi.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.Cart;
import com.example.demoapi.model.CheckoutOrder;
import com.example.demoapi.model.CheckoutReceipt;
import com.example.demoapi.model.CheckoutRequest;
import com.example.demoapi.model.CheckoutWhitelist;
import com.example.demoapi.model.TrackingDetails;
import com.example.demoapi.model.User;

@Service
public class CheckoutService {

    private static final List<String> WHITELISTED_ADDRESSES = List.of(
            "123 Main St, New York, NY 10001",
            "456 Oak Ave, Brooklyn, NY 11201",
            "789 Pine Rd, Queens, NY 11101",
            "1600 Pennsylvania Ave NW, Washington, DC 20500",
            "1 Infinite Loop, Cupertino, CA 95014");

    private static final List<String> WHITELISTED_CARDS = List.of(
            "4111111111111111",
            "4000056655665556",
            "5555555555554444",
            "6011111111111117",
            "378282246310005");

    private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a z", Locale.US);

    private final AtomicLong nextCheckoutOrderId = new AtomicLong(1);
    private final List<CheckoutOrder> checkoutOrders = new ArrayList<>();
    private final UserService userService;
    private final CartService cartService;
    private final OrderNotificationService orderNotificationService;

    public CheckoutService(
            UserService userService,
            CartService cartService,
            OrderNotificationService orderNotificationService) {
        this.userService = userService;
        this.cartService = cartService;
        this.orderNotificationService = orderNotificationService;
    }

    public CheckoutWhitelist getWhitelist() {
        return new CheckoutWhitelist(WHITELISTED_ADDRESSES, WHITELISTED_CARDS);
    }

    public CheckoutReceipt checkout(Long userId, CheckoutRequest request) {
        validateCheckoutRequest(request);

        User user = userService.findUserById(userId);
        Cart cart = cartService.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart must contain at least one item before checkout.");
        }

        String shippingAddress = request.shippingAddress().trim();
        String cardNumber = request.cardNumber().trim();

        if (!WHITELISTED_ADDRESSES.contains(shippingAddress)) {
            throw new IllegalArgumentException("Shipping address is not on the whitelist.");
        }
        if (!WHITELISTED_CARDS.contains(cardNumber)) {
            throw new IllegalArgumentException("Card number is not on the whitelist.");
        }

        long orderId = nextCheckoutOrderId.getAndIncrement();
        String orderTimeEst = ZonedDateTime.now(EST_ZONE).format(ORDER_TIME_FORMATTER);
        CheckoutOrder order = new CheckoutOrder(
                orderId,
                user.id(),
                user.firstName() + " " + user.lastName(),
                user.email(),
                shippingAddress,
                user.phone(),
                cart.items(),
                cart.totalAmount(),
                generateTrackingNumber(user.id(), orderId),
                "CONFIRMED",
                orderTimeEst);

        checkoutOrders.add(order);
        boolean emailSent = orderNotificationService.sendTrackingEmail(user, order);
        cartService.clearCart(userId);

        return new CheckoutReceipt(
                order.id(),
                user.id(),
                user.email(),
                order.shippingAddress(),
                maskCard(cardNumber),
                order.trackingNumber(),
                order.status(),
                order.items(),
                order.totalAmount(),
                emailSent,
                order.orderTimeEst());
    }

    public List<CheckoutOrder> getOrdersForUser(Long userId) {
        userService.findUserById(userId);
        return checkoutOrders.stream()
                .filter(order -> order.userId().equals(userId))
                .toList();
    }

    public TrackingDetails getTracking(String trackingNumber) {
        CheckoutOrder order = checkoutOrders.stream()
                .filter(entry -> entry.trackingNumber().equalsIgnoreCase(trackingNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tracking number " + trackingNumber + " was not found."));

        return new TrackingDetails(order.id(), order.trackingNumber(), order.status(), order.email());
    }

    public void reset() {
        checkoutOrders.clear();
        nextCheckoutOrderId.set(1);
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.shippingAddress() == null || request.shippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address is required.");
        }
        if (request.cardNumber() == null || request.cardNumber().isBlank()) {
            throw new IllegalArgumentException("Card number is required.");
        }
    }

    private String generateTrackingNumber(Long userId, Long orderId) {
        return ("TRK-" + userId + "-" + orderId).toUpperCase(Locale.US);
    }

    private String maskCard(String cardNumber) {
        int visibleDigits = Math.min(4, cardNumber.length());
        return "*".repeat(Math.max(0, cardNumber.length() - visibleDigits))
                + cardNumber.substring(cardNumber.length() - visibleDigits);
    }
}
