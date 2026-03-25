package com.example.demoapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.AddCartItemRequest;
import com.example.demoapi.model.Cart;
import com.example.demoapi.model.CartItem;
import com.example.demoapi.model.MenuItem;
import com.example.demoapi.model.UpdateCartItemRequest;

@Service
public class CartService {

    private final Map<Long, Map<Long, Integer>> cartsByUserId = new LinkedHashMap<>();
    private final UserService userService;
    private final MenuService menuService;

    public CartService(UserService userService, MenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    public Cart getCart(Long userId) {
        userService.findUserById(userId);

        Map<Long, Integer> quantities = cartsByUserId.getOrDefault(userId, Map.of());
        List<CartItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            MenuItem menuItem = menuService.getMenuItem(entry.getKey());
            int quantity = entry.getValue();
            BigDecimal lineTotal = menuItem.price().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartItem(menuItem.id(), menuItem.name(), menuItem.price(), quantity, lineTotal));
        }

        BigDecimal total = items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Cart(userId, items, total);
    }

    public Cart addItem(Long userId, AddCartItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.menuItemId() == null) {
            throw new IllegalArgumentException("Menu item id is required.");
        }
        int quantity = validateQuantity(request.quantity());

        userService.findUserById(userId);
        menuService.getMenuItem(request.menuItemId());

        Map<Long, Integer> cart = cartsByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
        cart.merge(request.menuItemId(), quantity, Integer::sum);
        return getCart(userId);
    }

    public Cart updateItem(Long userId, Long menuItemId, UpdateCartItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        userService.findUserById(userId);
        Map<Long, Integer> cart = cartsByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
        if (!cart.containsKey(menuItemId)) {
            throw new IllegalArgumentException("Cart item with menu item id " + menuItemId + " was not found.");
        }

        int quantity = validateQuantity(request.quantity());
        cart.put(menuItemId, quantity);
        return getCart(userId);
    }

    public void removeItem(Long userId, Long menuItemId) {
        userService.findUserById(userId);
        Map<Long, Integer> cart = cartsByUserId.get(userId);
        if (cart == null || !cart.containsKey(menuItemId)) {
            throw new IllegalArgumentException("Cart item with menu item id " + menuItemId + " was not found.");
        }
        cart.remove(menuItemId);
    }

    public void clearCart(Long userId) {
        cartsByUserId.remove(userId);
    }

    public void reset() {
        cartsByUserId.clear();
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        return quantity;
    }
}
