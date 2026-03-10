const tg = window.Telegram.WebApp;

tg.ready();
tg.expand();

const state = {
    products: [],
    cart: {},
    view: "catalog"
};

const API_BASE = "";

applyTelegramTheme();

async function apiFetch(path, options = {}) {
    const isJsonBody = Boolean(options.body);
    const headers = {
        "X-Telegram-Init-Data": tg.initData || "",
        ...(isJsonBody ? {"Content-Type": "application/json"} : {}),
        ...(options.headers || {})
    };

    const response = await fetch(API_BASE + path, {
        ...options,
        headers
    });

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || body.message || `HTTP ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function showView(name) {
    document.querySelectorAll(".view").forEach((viewEl) => viewEl.classList.add("hidden"));
    const targetView = document.getElementById("view-" + name);
    if (targetView) {
        targetView.classList.remove("hidden");
    }
    state.view = name;
    updateMainButton();
}

async function loadCatalog() {
    const products = await apiFetch("/api/products");
    state.products = Array.isArray(products) ? products : [];
    renderCatalog();
}

function renderCatalog() {
    const container = document.getElementById("product-list");
    container.innerHTML = "";

    state.products.forEach((product) => {
        const card = document.createElement("div");
        card.className = "product-card";
        const qty = state.cart[product.id] || 0;

        card.innerHTML = `
            <div class="product-info">
                <div class="product-name">${escapeHtml(product.name)}</div>
                <div class="product-price">${formatPrice(product.price)} ₫</div>
                ${product.description ? `<div class="product-desc">${escapeHtml(product.description)}</div>` : ""}
            </div>
            <div class="product-controls">
                ${qty > 0
                    ? `<button class="btn-minus" data-id="${product.id}" type="button">−</button>
                       <span class="qty">${qty}</span>`
                    : ""}
                <button class="btn-plus" data-id="${product.id}" type="button">+</button>
            </div>
        `;

        container.appendChild(card);
    });

    container.querySelectorAll(".btn-plus").forEach((button) => {
        button.addEventListener("click", () => changeQty(Number(button.dataset.id), 1));
    });

    container.querySelectorAll(".btn-minus").forEach((button) => {
        button.addEventListener("click", () => changeQty(Number(button.dataset.id), -1));
    });
}

function changeQty(productId, delta) {
    const current = state.cart[productId] || 0;
    const next = Math.max(0, current + delta);
    if (next === 0) {
        delete state.cart[productId];
    } else {
        state.cart[productId] = next;
    }
    renderCatalog();
    updateMainButton();
}

function cartTotal() {
    return Object.entries(state.cart).reduce((sum, [id, qty]) => {
        const product = state.products.find((p) => p.id === Number(id));
        return sum + (product ? product.price * qty : 0);
    }, 0);
}

function cartItemCount() {
    return Object.values(state.cart).reduce((sum, qty) => sum + qty, 0);
}

function openCheckout() {
    if (cartItemCount() === 0) {
        return;
    }

    const summary = document.getElementById("cart-summary");
    const items = Object.entries(state.cart).map(([id, qty]) => {
        const product = state.products.find((p) => p.id === Number(id));
        if (!product) {
            return "";
        }
        return `<div class="cart-item"><span>${escapeHtml(product.name)} × ${qty}</span><span>${formatPrice(product.price * qty)} ₫</span></div>`;
    }).filter(Boolean);
    items.push(`<div class="cart-total">Итого: ${formatPrice(cartTotal())} ₫</div>`);
    summary.innerHTML = items.join("");

    const nameInput = document.getElementById("input-name");
    if (!nameInput.value && tg.initDataUnsafe && tg.initDataUnsafe.user) {
        const user = tg.initDataUnsafe.user;
        nameInput.value = [user.first_name, user.last_name].filter(Boolean).join(" ");
    }

    showView("checkout");
}

async function submitOrder() {
    const name = document.getElementById("input-name").value.trim();
    if (!name) {
        tg.showAlert("Укажите ваше имя");
        return;
    }
    if (cartItemCount() === 0) {
        tg.showAlert("Корзина пуста");
        return;
    }

    const rawPhone = document.getElementById("input-phone").value.trim();
    const rawNote = document.getElementById("input-note").value.trim();

    const payload = {
        customerName: name,
        customerPhone: rawPhone || null,
        note: rawNote || null,
        items: Object.entries(state.cart).map(([id, qty]) => ({
            productId: Number(id),
            quantity: qty
        }))
    };

    tg.MainButton.showProgress(false);

    try {
        const order = await apiFetch("/api/orders", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        const dateStr = order && order.deliveryDate ? formatDate(order.deliveryDate) : "";
        document.getElementById("confirmation-text").textContent =
            `Заказ #${order.id} принят.${dateStr ? ` Дата доставки: ${dateStr}` : ""}`;
        showView("confirmation");
    } catch (error) {
        tg.showAlert("Ошибка: " + error.message);
    } finally {
        tg.MainButton.hideProgress();
    }
}

function updateMainButton() {
    const button = tg.MainButton;

    if (state.view === "catalog") {
        const count = cartItemCount();
        if (count > 0) {
            button.setText(`Оформить заказ (${formatPrice(cartTotal())} ₫)`);
            button.show();
            button.enable();
        } else {
            button.hide();
        }
        return;
    }

    if (state.view === "checkout") {
        button.setText("Подтвердить заказ");
        button.show();
        button.enable();
        return;
    }

    button.hide();
}

function applyTelegramTheme() {
    const root = document.documentElement;
    const params = tg.themeParams || {};
    const mappings = {
        bg_color: "--tg-theme-bg-color",
        text_color: "--tg-theme-text-color",
        hint_color: "--tg-theme-hint-color",
        button_color: "--tg-theme-button-color",
        button_text_color: "--tg-theme-button-text-color",
        secondary_bg_color: "--tg-theme-secondary-bg-color"
    };

    Object.entries(mappings).forEach(([key, cssVar]) => {
        if (params[key]) {
            root.style.setProperty(cssVar, params[key]);
        }
    });
}

function escapeHtml(value) {
    const str = String(value ?? "");
    return str
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function formatPrice(value) {
    return Number(value || 0).toLocaleString("ru-RU");
}

function formatDate(isoDate) {
    const [year, month, day] = String(isoDate).split("-");
    if (!year || !month || !day) {
        return String(isoDate);
    }
    return `${day}.${month}.${year}`;
}

tg.MainButton.onClick(() => {
    if (state.view === "catalog") {
        openCheckout();
    } else if (state.view === "checkout") {
        submitOrder();
    }
});

document.getElementById("btn-close").addEventListener("click", () => tg.close());

loadCatalog().catch((error) => {
    const container = document.getElementById("product-list");
    container.textContent = "Ошибка загрузки каталога: " + error.message;
});
