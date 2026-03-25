# Postman URLs

Base URL: `http://localhost:8080`

Frontend pages:
- `GET /index.html`
- `GET /login.html`
- `GET /forgot-password.html`
- `GET /reset-password.html?token=<token>`
- `GET /account-confirmed.html?token=<token>`
- `GET /profile.html`
- `GET /menu.html`
- `GET /checkout.html`
- `GET /confirmation.html`
- `GET /order-history.html`
- `GET /order-status.html?tracking=TRK-1-1`

Auth:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/confirm-email`

Users:
- `GET /api/users`
- `GET /api/users/{userId}`
- `PUT /api/users/{userId}/profile`
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

Sample forgot-password body:

```json
{
  "email": "ava@example.com"
}
```

Sample reset-password body:

```json
{
  "token": "paste-token-from-mailhog-link",
  "password": "newsecret123",
  "confirmPassword": "newsecret123"
}
```

Sample confirm-email body:

```json
{
  "token": "paste-token-from-mailhog-link"
}
```

Sample profile update body:

```json
{
  "firstName": "Ava",
  "lastName": "Johnson",
  "address": "500 Park Ave, New York, NY 10022",
  "zip": "10022",
  "phone": "555-999-1234",
  "email": "ava.updated@example.com"
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
