package com.example.demoapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demoapi.service.CartService;
import com.example.demoapi.service.CheckoutService;
import com.example.demoapi.service.MenuService;
import com.example.demoapi.service.OrderService;
import com.example.demoapi.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuService menuService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CheckoutService checkoutService;

    @BeforeEach
    void resetState() {
        menuService.reset();
        orderService.reset();
        userService.reset();
        cartService.reset();
        checkoutService.reset();
    }

    @Test
    void helloEndpointReturnsMessage() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello from Spring Boot!"));
    }

    @Test
    void healthEndpointReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void menuEndpointReturnsSeededMenuItems() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cheeseburger"))
                .andExpect(jsonPath("$[1].name").value("Chicken Alfredo Pasta"))
                .andExpect(jsonPath("$[2].name").value("Caesar Salad"));
    }

    @Test
    void getMenuItemReturnsSingleMenuItem() throws Exception {
        mockMvc.perform(get("/api/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cheeseburger"));
    }

    @Test
    void addMenuItemCreatesNewFoodItem() throws Exception {
        mockMvc.perform(post("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Margherita Pizza",
                                  "description": "Classic pizza with tomato, mozzarella, and basil",
                                  "price": 13.75
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Margherita Pizza"));
    }

    @Test
    void patchMenuItemUpdatesRequestedFields() throws Exception {
        mockMvc.perform(patch("/api/menu/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "price": 14.25,
                                  "description": "Creamy pasta with grilled chicken and parmesan"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Chicken Alfredo Pasta"))
                .andExpect(jsonPath("$.description").value("Creamy pasta with grilled chicken and parmesan"))
                .andExpect(jsonPath("$.price").value(14.25));
    }

    @Test
    void deleteMenuItemRemovesItFromMenu() throws Exception {
        mockMvc.perform(delete("/api/menu/3"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void deleteMenuItemRejectsUnknownId() throws Exception {
        mockMvc.perform(delete("/api/menu/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Menu item with id 99 was not found."));
    }

    @Test
    void placeOrderCreatesOrderForSelectedMenuItems() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Ava"))
                .andExpect(jsonPath("$.address").value("123 Main St, New York, NY"))
                .andExpect(jsonPath("$.phoneNumber").value("555-123-4567"))
                .andExpect(jsonPath("$.items[0].name").value("Cheeseburger"))
                .andExpect(jsonPath("$.items[1].name").value("Caesar Salad"))
                .andExpect(jsonPath("$.totalAmount").value(16.24));
    }

    @Test
    void placeOrderRejectsUnknownMenuItem() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [99]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Menu item with id 99 was not found."));
    }

    @Test
    void patchOrderUpdatesNameAddressPhoneAndItems() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava Johnson",
                                  "address": "456 Oak Ave, Brooklyn, NY",
                                  "phoneNumber": "555-000-9999",
                                  "itemIds": [2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Ava Johnson"))
                .andExpect(jsonPath("$.address").value("456 Oak Ave, Brooklyn, NY"))
                .andExpect(jsonPath("$.phoneNumber").value("555-000-9999"))
                .andExpect(jsonPath("$.items[0].name").value("Chicken Alfredo Pasta"))
                .andExpect(jsonPath("$.totalAmount").value(12.50));
    }

    @Test
    void putOrderReplacesEntireOrderDetails() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Noah",
                                  "address": "789 Pine Rd, Queens, NY",
                                  "phoneNumber": "555-222-3333",
                                  "itemIds": [2, 3]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Noah"))
                .andExpect(jsonPath("$.address").value("789 Pine Rd, Queens, NY"))
                .andExpect(jsonPath("$.phoneNumber").value("555-222-3333"))
                .andExpect(jsonPath("$.items[0].name").value("Chicken Alfredo Pasta"))
                .andExpect(jsonPath("$.items[1].name").value("Caesar Salad"))
                .andExpect(jsonPath("$.totalAmount").value(19.75));
    }

    @Test
    void getOrderReturnsSingleOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Ava"))
                .andExpect(jsonPath("$.items[1].name").value("Caesar Salad"));
    }

    @Test
    void getOrderFieldEndpointsReturnScalarValues() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/1/customerName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Ava"));

        mockMvc.perform(get("/api/orders/1/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("123 Main St, New York, NY"));

        mockMvc.perform(get("/api/orders/1/phoneNumber"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("555-123-4567"));

        mockMvc.perform(get("/api/orders/1/totalAmount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("16.24"));
    }

    @Test
    void getOrderItemsReturnsItemsForOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    void getOrderItemReturnsSingleItemFromOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cheeseburger"));
    }

    @Test
    void getOrderItemRejectsMissingItemInOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/1/items/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Order item with id 2 was not found in order 1."));
    }

    @Test
    void deleteOrderRemovesExistingOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ava",
                                  "address": "123 Main St, New York, NY",
                                  "phoneNumber": "555-123-4567",
                                  "itemIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void registerLoginCartAndCheckoutFlowWorks() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ava",
                                  "lastName": "Johnson",
                                  "address": "123 Main St, New York, NY 10001",
                                  "zip": "10001",
                                  "phone": "555-123-4567",
                                  "email": "ava@example.com",
                                  "password": "secret123",
                                  "confirmPassword": "secret123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ava@example.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ava@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("Ava"));

        mockMvc.perform(post("/api/users/1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "menuItemId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].menuItemId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(17.98));

        mockMvc.perform(get("/api/checkout/whitelist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses.length()").value(5))
                .andExpect(jsonPath("$.cards.length()").value(5));

        mockMvc.perform(post("/api/users/1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": "123 Main St, New York, NY 10001",
                                  "cardNumber": "4111111111111111"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.trackingNumber").value("TRK-1-1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.maskedCard").value("************1111"));

        mockMvc.perform(get("/api/tracking/TRK-1-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TRK-1-1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.email").value("ava@example.com"));
    }

    @Test
    void checkoutRejectsNonWhitelistedCard() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Noah",
                                  "lastName": "Lee",
                                  "address": "456 Oak Ave, Brooklyn, NY 11201",
                                  "zip": "11201",
                                  "phone": "555-222-3333",
                                  "email": "noah@example.com",
                                  "password": "secret123",
                                  "confirmPassword": "secret123"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "menuItemId": 2,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": "456 Oak Ave, Brooklyn, NY 11201",
                                  "cardNumber": "9999888877776666"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Card number is not on the whitelist."));
    }
}
