package com.example.demoapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demoapi.model.MenuItem;
import com.example.demoapi.model.Order;
import com.example.demoapi.model.PatchOrderRequest;
import com.example.demoapi.model.PlaceOrderRequest;
import com.example.demoapi.model.UpdateOrderRequest;
import com.example.demoapi.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/{id}/customerName")
    public String getOrderCustomerName(@PathVariable Long id) {
        return orderService.getOrderCustomerName(id);
    }

    @GetMapping("/{id}/address")
    public String getOrderAddress(@PathVariable Long id) {
        return orderService.getOrderAddress(id);
    }

    @GetMapping("/{id}/phoneNumber")
    public String getOrderPhoneNumber(@PathVariable Long id) {
        return orderService.getOrderPhoneNumber(id);
    }

    @GetMapping("/{id}/totalAmount")
    public String getOrderTotalAmount(@PathVariable Long id) {
        return orderService.getOrderTotalAmount(id).toPlainString();
    }

    @GetMapping("/{id}/items")
    public List<MenuItem> getOrderItems(@PathVariable Long id) {
        return orderService.getOrderItems(id);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public MenuItem getOrderItem(@PathVariable Long orderId, @PathVariable Long itemId) {
        return orderService.getOrderItem(orderId, itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @PatchMapping("/{id}")
    public Order patchOrder(@PathVariable Long id, @RequestBody PatchOrderRequest request) {
        return orderService.patchOrder(id, request);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        return orderService.updateOrder(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
