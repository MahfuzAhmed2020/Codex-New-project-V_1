package com.example.demoapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.MenuItem;
import com.example.demoapi.model.Order;
import com.example.demoapi.model.PatchOrderRequest;
import com.example.demoapi.model.PlaceOrderRequest;
import com.example.demoapi.model.UpdateOrderRequest;

@Service
public class OrderService {

    private final AtomicLong nextOrderId = new AtomicLong(1);
    private final List<Order> orders = new ArrayList<>();
    private final MenuService menuService;

    public OrderService(MenuService menuService) {
        this.menuService = menuService;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    public Order getOrder(Long orderId) {
        return findOrderById(orderId);
    }

    public String getOrderCustomerName(Long orderId) {
        return findOrderById(orderId).customerName();
    }

    public String getOrderAddress(Long orderId) {
        return findOrderById(orderId).address();
    }

    public String getOrderPhoneNumber(Long orderId) {
        return findOrderById(orderId).phoneNumber();
    }

    public BigDecimal getOrderTotalAmount(Long orderId) {
        return findOrderById(orderId).totalAmount();
    }

    public List<MenuItem> getOrderItems(Long orderId) {
        return findOrderById(orderId).items();
    }

    public MenuItem getOrderItem(Long orderId, Long itemId) {
        return findOrderById(orderId).items().stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order item with id " + itemId + " was not found in order " + orderId + "."));
    }

    public Order placeOrder(PlaceOrderRequest request) {
        validatePlaceOrderRequest(request);

        List<MenuItem> selectedItems = menuService.getMenuItemsByIds(request.itemIds());
        Order order = buildOrder(
                nextOrderId.getAndIncrement(),
                request.customerName().trim(),
                request.address().trim(),
                request.phoneNumber().trim(),
                selectedItems);

        orders.add(order);
        return order;
    }

    public Order patchOrder(Long orderId, PatchOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        Order existingOrder = findOrderById(orderId);
        String customerName = existingOrder.customerName();
        String address = existingOrder.address();
        String phoneNumber = existingOrder.phoneNumber();
        List<MenuItem> items = existingOrder.items();
        boolean changed = false;

        if (request.customerName() != null) {
            if (request.customerName().isBlank()) {
                throw new IllegalArgumentException("Customer name cannot be blank.");
            }
            customerName = request.customerName().trim();
            changed = true;
        }

        if (request.address() != null) {
            if (request.address().isBlank()) {
                throw new IllegalArgumentException("Address cannot be blank.");
            }
            address = request.address().trim();
            changed = true;
        }

        if (request.phoneNumber() != null) {
            if (request.phoneNumber().isBlank()) {
                throw new IllegalArgumentException("Phone number cannot be blank.");
            }
            phoneNumber = request.phoneNumber().trim();
            changed = true;
        }

        if (request.itemIds() != null) {
            items = menuService.getMenuItemsByIds(request.itemIds());
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("At least one field must be provided for update.");
        }

        Order updatedOrder = buildOrder(existingOrder.id(), customerName, address, phoneNumber, items);
        replaceOrder(existingOrder, updatedOrder);
        return updatedOrder;
    }

    public Order updateOrder(Long orderId, UpdateOrderRequest request) {
        validateUpdateOrderRequest(request);

        Order existingOrder = findOrderById(orderId);
        List<MenuItem> items = menuService.getMenuItemsByIds(request.itemIds());
        Order updatedOrder = buildOrder(
                existingOrder.id(),
                request.customerName().trim(),
                request.address().trim(),
                request.phoneNumber().trim(),
                items);
        replaceOrder(existingOrder, updatedOrder);
        return updatedOrder;
    }

    public void deleteOrder(Long orderId) {
        Order existingOrder = findOrderById(orderId);
        orders.remove(existingOrder);
    }

    public void reset() {
        orders.clear();
        nextOrderId.set(1);
    }

    private void validatePlaceOrderRequest(PlaceOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.customerName() == null || request.customerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("Address is required.");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (request.itemIds() == null || request.itemIds().isEmpty()) {
            throw new IllegalArgumentException("At least one menu item must be selected.");
        }
    }

    private void validateUpdateOrderRequest(UpdateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.customerName() == null || request.customerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("Address is required.");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (request.itemIds() == null || request.itemIds().isEmpty()) {
            throw new IllegalArgumentException("At least one menu item must be selected.");
        }
    }

    private Order findOrderById(Long orderId) {
        return orders.stream()
                .filter(order -> order.id().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order with id " + orderId + " was not found."));
    }

    private Order buildOrder(Long id, String customerName, String address, String phoneNumber, List<MenuItem> items) {
        BigDecimal total = items.stream()
                .map(MenuItem::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(id, customerName, address, phoneNumber, items, total);
    }

    private void replaceOrder(Order existingOrder, Order updatedOrder) {
        int index = orders.indexOf(existingOrder);
        orders.set(index, updatedOrder);
    }
}
