const state = {
  apiBase: "/api",
};

function saveSession(session) {
  sessionStorage.setItem("demoUser", JSON.stringify(session));
}

function getSession() {
  const raw = sessionStorage.getItem("demoUser");
  return raw ? JSON.parse(raw) : null;
}

function saveReceipt(receipt) {
  sessionStorage.setItem("demoReceipt", JSON.stringify(receipt));
}

function getReceipt() {
  const raw = sessionStorage.getItem("demoReceipt");
  return raw ? JSON.parse(raw) : null;
}

async function api(path, options = {}) {
  const response = await fetch(`${state.apiBase}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    let message = "Request failed.";
    try {
      const body = await response.json();
      message = body.error || message;
    } catch (error) {
      message = response.statusText || message;
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

function navigateTo(path) {
  window.location.assign(path);
}

function renderMessage(targetId, message, isError = false) {
  const target = document.getElementById(targetId);
  if (!target) {
    return;
  }
  target.textContent = message;
  target.className = isError ? "status-box error-box" : "status-box";
  target.hidden = false;
}

function clearMessage(targetId) {
  const target = document.getElementById(targetId);
  if (target) {
    target.hidden = true;
    target.textContent = "";
  }
}

function attachPageNavigation() {
  document.querySelectorAll("[data-nav]").forEach((button) => {
    button.addEventListener("click", () => {
      const fallback = button.dataset.fallback || "/index.html";
      navigateTo(fallback);
    });
  });
}

function getTrackingFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get("tracking");
}

async function handleRegister(event) {
  event.preventDefault();
  clearMessage("register-message");

  const form = event.currentTarget;
  const payload = Object.fromEntries(new FormData(form).entries());

  try {
    await api("/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    renderMessage("register-message", "Registration successful. Continue to the login page.");
    form.reset();
    setTimeout(() => {
      navigateTo("/login.html");
    }, 900);
  } catch (error) {
    renderMessage("register-message", error.message, true);
  }
}

async function handleLogin(event) {
  event.preventDefault();
  clearMessage("login-message");

  const form = event.currentTarget;
  const payload = Object.fromEntries(new FormData(form).entries());

  try {
    const session = await api("/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    saveSession(session);
    navigateTo("/menu.html");
  } catch (error) {
    renderMessage("login-message", error.message, true);
  }
}

async function loadMenuPage() {
  const session = getSession();
  if (!session) {
    navigateTo("/login.html");
    return;
  }

  document.getElementById("welcome-name").textContent = `${session.firstName} ${session.lastName}`;

  const [menu, cart] = await Promise.all([
    api("/menu"),
    api(`/users/${session.userId}/cart`),
  ]);

  const menuContainer = document.getElementById("menu-items");
  menuContainer.innerHTML = "";

  menu.forEach((item) => {
    const card = document.createElement("article");
    card.className = "menu-card";
    card.innerHTML = `
      <span class="pill">Item #${item.id}</span>
      <h3>${item.name}</h3>
      <p class="muted">${item.description}</p>
      <p class="price">$${Number(item.price).toFixed(2)}</p>
      <label>
        Quantity
        <input type="number" min="1" value="1" id="qty-${item.id}">
      </label>
      <button type="button" data-item-id="${item.id}">Add to cart</button>
    `;
    menuContainer.appendChild(card);
  });

  menuContainer.querySelectorAll("button[data-item-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      clearMessage("menu-message");
      const menuItemId = Number(button.dataset.itemId);
      const quantity = Number(document.getElementById(`qty-${menuItemId}`).value || 1);

      try {
        const updatedCart = await api(`/users/${session.userId}/cart/items`, {
          method: "POST",
          body: JSON.stringify({ menuItemId, quantity }),
        });
        renderCart(updatedCart);
        renderMessage("menu-message", "Item added to cart.");
      } catch (error) {
        renderMessage("menu-message", error.message, true);
      }
    });
  });

  renderCart(cart);
}

function renderCart(cart) {
  const cartList = document.getElementById("cart-items");
  const cartTotal = document.getElementById("cart-total");

  if (!cartList || !cartTotal) {
    return;
  }

  cartList.innerHTML = "";
  if (!cart.items.length) {
    cartList.innerHTML = `<li class="cart-row"><span>Your cart is empty.</span><span>Add menu items to continue.</span></li>`;
  } else {
    cart.items.forEach((item) => {
      const row = document.createElement("li");
      row.className = "cart-row";
      row.innerHTML = `
        <span>${item.name} x ${item.quantity}</span>
        <span>$${Number(item.lineTotal).toFixed(2)}</span>
      `;
      cartList.appendChild(row);
    });
  }

  cartTotal.textContent = `$${Number(cart.totalAmount).toFixed(2)}`;
}

async function loadCheckoutPage() {
  const session = getSession();
  if (!session) {
    navigateTo("/login.html");
    return;
  }

  const [whitelist, cart] = await Promise.all([
    api("/checkout/whitelist"),
    api(`/users/${session.userId}/cart`),
  ]);

  if (!cart.items.length) {
    navigateTo("/menu.html");
    return;
  }

  const addressSelect = document.getElementById("shippingAddress");
  const cardSelect = document.getElementById("cardNumber");

  whitelist.addresses.forEach((address) => {
    const option = document.createElement("option");
    option.value = address;
    option.textContent = address;
    addressSelect.appendChild(option);
  });

  whitelist.cards.forEach((card) => {
    const option = document.createElement("option");
    option.value = card;
    option.textContent = `${"*".repeat(Math.max(0, card.length - 4))}${card.slice(-4)}`;
    cardSelect.appendChild(option);
  });

  const summary = document.getElementById("checkout-summary");
  summary.innerHTML = "";
  cart.items.forEach((item) => {
    const row = document.createElement("li");
    row.className = "summary-row";
    row.innerHTML = `<span>${item.name} x ${item.quantity}</span><span>$${Number(item.lineTotal).toFixed(2)}</span>`;
    summary.appendChild(row);
  });
  document.getElementById("checkout-total").textContent = `$${Number(cart.totalAmount).toFixed(2)}`;

  document.getElementById("checkout-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessage("checkout-message");

    try {
      const receipt = await api(`/users/${session.userId}/checkout`, {
        method: "POST",
        body: JSON.stringify({
          shippingAddress: addressSelect.value,
          cardNumber: cardSelect.value,
        }),
      });
      saveReceipt(receipt);
      navigateTo("/order-history.html");
    } catch (error) {
      renderMessage("checkout-message", error.message, true);
    }
  });
}

function renderOrderHistoryCard(order, isLatest) {
  const itemsMarkup = order.items
    .map((item) => `<li class="summary-row"><span>${item.name} x ${item.quantity}</span><span>$${Number(item.lineTotal).toFixed(2)}</span></li>`)
    .join("");

  return `
    <article class="panel history-card">
      <div class="button-row">
        <span class="pill">Order #${order.id}</span>
        ${isLatest ? '<span class="pill">Latest</span>' : ""}
      </div>
      <p><strong>Status:</strong> ${order.status}</p>
      <p><strong>Tracking:</strong> <a class="inline-link" href="/order-status.html?tracking=${order.trackingNumber}">${order.trackingNumber}</a></p>
      <p><strong>Email:</strong> ${order.email}</p>
      <p><strong>Shipping:</strong> ${order.shippingAddress}</p>
      <p><strong>Total:</strong> $${Number(order.totalAmount).toFixed(2)}</p>
      <ul class="summary-list">${itemsMarkup}</ul>
    </article>
  `;
}

async function loadOrderHistoryPage() {
  const session = getSession();
  if (!session) {
    navigateTo("/login.html");
    return;
  }

  const orders = await api(`/users/${session.userId}/orders`);
  const receipt = getReceipt();
  const historyContainer = document.getElementById("order-history-list");
  const latestSummary = document.getElementById("latest-order-summary");

  document.getElementById("history-name").textContent = `${session.firstName} ${session.lastName}`;
  historyContainer.innerHTML = "";

  if (!orders.length) {
    latestSummary.innerHTML = `<div class="status-box">No orders yet. Place an order from the menu to see it here.</div>`;
    return;
  }

  const latestOrder = orders[orders.length - 1];
  latestSummary.innerHTML = `
    <div class="panel">
      <h2>Latest Order</h2>
      <p><strong>Tracking number:</strong> <a class="inline-link" href="/order-status.html?tracking=${latestOrder.trackingNumber}">${latestOrder.trackingNumber}</a></p>
      <p><strong>Status:</strong> ${latestOrder.status}</p>
      <p><strong>Total:</strong> $${Number(latestOrder.totalAmount).toFixed(2)}</p>
      <p><strong>Shipping:</strong> ${latestOrder.shippingAddress}</p>
      <p><strong>Email update:</strong> ${receipt && receipt.trackingNumber === latestOrder.trackingNumber && receipt.emailSent ? "Sent to MailHog." : "Available in order details."}</p>
    </div>
  `;

  const reversed = [...orders].reverse();
  historyContainer.innerHTML = reversed
    .map((order) => renderOrderHistoryCard(order, order.id === latestOrder.id))
    .join("");
}

async function loadOrderStatusPage() {
  const trackingNumber = getTrackingFromUrl();
  const statusBox = document.getElementById("tracking-status-box");
  const detailsBox = document.getElementById("tracking-details");

  if (!trackingNumber) {
    statusBox.innerHTML = `<div class="status-box error-box">Tracking number is missing from the URL.</div>`;
    return;
  }

  try {
    const tracking = await api(`/tracking/${trackingNumber}`);
    document.getElementById("tracking-page-number").textContent = tracking.trackingNumber;
    detailsBox.innerHTML = `
      <div class="panel history-card">
        <p><strong>Order ID:</strong> ${tracking.orderId}</p>
        <p><strong>Tracking Number:</strong> ${tracking.trackingNumber}</p>
        <p><strong>Status:</strong> ${tracking.status}</p>
        <p><strong>Email:</strong> ${tracking.email}</p>
      </div>
    `;
    statusBox.innerHTML = `<div class="status-box">Order status loaded successfully.</div>`;
  } catch (error) {
    statusBox.innerHTML = `<div class="status-box error-box">${error.message}</div>`;
  }
}

function loadConfirmationPage() {
  const receipt = getReceipt();
  if (!receipt) {
    navigateTo("/menu.html");
    return;
  }

  document.getElementById("tracking-number").textContent = receipt.trackingNumber;
  document.getElementById("receipt-email").textContent = receipt.email;
  document.getElementById("receipt-status").textContent = receipt.status;
  document.getElementById("receipt-address").textContent = receipt.shippingAddress;
  document.getElementById("receipt-card").textContent = receipt.maskedCard;
  document.getElementById("receipt-total").textContent = `$${Number(receipt.totalAmount).toFixed(2)}`;
  document.getElementById("mailhog-note").innerHTML = receipt.emailSent
    ? `Tracking email sent to MailHog. Open <a class="inline-link" href="/order-status.html?tracking=${receipt.trackingNumber}">order status</a>.`
    : "Tracking email could not be delivered. Start MailHog on localhost:1025 to capture it.";

  const summary = document.getElementById("confirmation-summary");
  summary.innerHTML = "";
  receipt.items.forEach((item) => {
    const row = document.createElement("li");
    row.className = "summary-row";
    row.innerHTML = `<span>${item.name} x ${item.quantity}</span><span>$${Number(item.lineTotal).toFixed(2)}</span>`;
    summary.appendChild(row);
  });
}

function attachLogout(buttonId) {
  const button = document.getElementById(buttonId);
  if (!button) {
    return;
  }

  button.addEventListener("click", () => {
    sessionStorage.removeItem("demoUser");
    sessionStorage.removeItem("demoReceipt");
    navigateTo("/login.html");
  });
}

window.addEventListener("DOMContentLoaded", () => {
  const page = document.body.dataset.page;
  attachPageNavigation();

  if (page === "register") {
    document.getElementById("register-form").addEventListener("submit", handleRegister);
  }

  if (page === "login") {
    document.getElementById("login-form").addEventListener("submit", handleLogin);
  }

  if (page === "menu") {
    loadMenuPage();
    attachLogout("logout-button");
  }

  if (page === "checkout") {
    loadCheckoutPage();
    attachLogout("checkout-logout-button");
  }

  if (page === "confirmation") {
    loadConfirmationPage();
    attachLogout("confirmation-logout-button");
  }

  if (page === "order-history") {
    loadOrderHistoryPage();
    attachLogout("history-logout-button");
  }

  if (page === "order-status") {
    loadOrderStatusPage();
  }
});
