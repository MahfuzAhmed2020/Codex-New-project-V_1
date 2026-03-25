package com.example.demoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demoapi.model.AddCartItemRequest;
import com.example.demoapi.model.Cart;
import com.example.demoapi.model.UpdateCartItemRequest;
import com.example.demoapi.service.CartService;

@RestController
@RequestMapping("/api/users/{userId}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Cart addCartItem(@PathVariable Long userId, @RequestBody AddCartItemRequest request) {
        return cartService.addItem(userId, request);
    }

    @PatchMapping("/items/{menuItemId}")
    public Cart updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long menuItemId,
            @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, menuItemId, request);
    }

    @DeleteMapping("/items/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCartItem(@PathVariable Long userId, @PathVariable Long menuItemId) {
        cartService.removeItem(userId, menuItemId);
    }
}
