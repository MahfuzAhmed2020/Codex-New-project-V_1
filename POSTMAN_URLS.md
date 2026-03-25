# Postman URLs

Base URL: `http://localhost:8080`

Frontend pages:
- `GET /index.html`
- `GET /login.html`
- `GET /menu.html`
- `GET /checkout.html`
- `GET /confirmation.html`
- `GET /order-history.html`
- `GET /order-status.html?tracking=TRK-1-1`

Auth:
- `POST /api/auth/register`
- `POST /api/auth/login`

Users:
- `GET /api/users`
- `GET /api/users/{userId}`
- `GET /api/users/{userId}/orders`

Menu:
- `GET /api/menu`
- `GET /api/menu/{id}`
- `POST /api/menu`
- `PATCH /api/menu/{id}`
- `DELETE /api/menu/{id}`

Cart:
- `GET /api/users/{userId}/cart`
- `POST /api/users/{userId}/cart/items`
- `PATCH /api/users/{userId}/cart/items/{menuItemId}`
- `DELETE /api/users/{userId}/cart/items/{menuItemId}`

Checkout and tracking:
- `GET /api/checkout/whitelist`
- `POST /api/users/{userId}/checkout`
- `GET /api/tracking/{trackingNumber}`
- Browser status page: `GET /order-status.html?tracking={trackingNumber}`

Legacy order CRUD:
- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders`
- `PATCH /api/orders/{id}`
- `PUT /api/orders/{id}`
- `DELETE /api/orders/{id}`

Sample registration body:

```json
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
```

Sample login body:

```json
{
  "email": "ava@example.com",
  "password": "secret123"
}
```

Sample add-to-cart body:

```json
{
  "menuItemId": 1,
  "quantity": 2
}
```

Sample checkout body:

```json
{
  "shippingAddress": "123 Main St, New York, NY 10001",
  "cardNumber": "4111111111111111"
}
```
