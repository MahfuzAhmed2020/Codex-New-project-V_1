package com.example.demoapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demoapi.model.CreateMenuItemRequest;
import com.example.demoapi.model.MenuItem;
import com.example.demoapi.model.UpdateMenuItemRequest;

@Service
public class MenuService {

    private final AtomicLong nextMenuId = new AtomicLong(4);
    private final List<MenuItem> menuItems = new ArrayList<>();

    public MenuService() {
        reset();
    }

    public List<MenuItem> getMenuItems() {
        return menuItems.stream()
                .sorted(Comparator.comparing(MenuItem::id))
                .toList();
    }

    public MenuItem getMenuItem(Long itemId) {
        return findMenuItemById(itemId);
    }

    public MenuItem addMenuItem(CreateMenuItemRequest request) {
        validateMenuItemRequest(request);

        MenuItem menuItem = new MenuItem(
                nextMenuId.getAndIncrement(),
                request.name().trim(),
                request.description().trim(),
                request.price());

        menuItems.add(menuItem);
        return menuItem;
    }

    public MenuItem updateMenuItem(Long itemId, UpdateMenuItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        MenuItem existingItem = findMenuItemById(itemId);
        String updatedName = existingItem.name();
        String updatedDescription = existingItem.description();
        BigDecimal updatedPrice = existingItem.price();
        boolean changed = false;

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new IllegalArgumentException("Menu item name cannot be blank.");
            }
            updatedName = request.name().trim();
            changed = true;
        }

        if (request.description() != null) {
            if (request.description().isBlank()) {
                throw new IllegalArgumentException("Menu item description cannot be blank.");
            }
            updatedDescription = request.description().trim();
            changed = true;
        }

        if (request.price() != null) {
            if (request.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Menu item price must be greater than zero.");
            }
            updatedPrice = request.price();
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("At least one field must be provided for update.");
        }

        MenuItem updatedItem = new MenuItem(existingItem.id(), updatedName, updatedDescription, updatedPrice);
        int index = menuItems.indexOf(existingItem);
        menuItems.set(index, updatedItem);
        return updatedItem;
    }

    public void deleteMenuItem(Long itemId) {
        MenuItem existingItem = findMenuItemById(itemId);
        menuItems.remove(existingItem);
    }

    public void reset() {
        menuItems.clear();
        menuItems.add(new MenuItem(1L, "Cheeseburger", "Juicy grilled beef patty with cheese", new BigDecimal("8.99")));
        menuItems.add(new MenuItem(2L, "Chicken Alfredo Pasta", "Creamy pasta with grilled chicken", new BigDecimal("12.50")));
        menuItems.add(new MenuItem(3L, "Caesar Salad", "Romaine lettuce, croutons, parmesan, and dressing", new BigDecimal("7.25")));
        nextMenuId.set(4);
    }

    public List<MenuItem> getMenuItemsByIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("At least one menu item must be selected.");
        }

        List<MenuItem> selectedItems = itemIds.stream()
                .map(this::findMenuItemById)
                .toList();

        return selectedItems;
    }

    private MenuItem findMenuItemById(Long itemId) {
        return menuItems.stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu item with id " + itemId + " was not found."));
    }

    private void validateMenuItemRequest(CreateMenuItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Menu item name is required.");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("Menu item description is required.");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Menu item price must be greater than zero.");
        }
    }
}
