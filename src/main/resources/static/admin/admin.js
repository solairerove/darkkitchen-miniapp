(function () {
    const state = {
        orderStatuses: ["PENDING", "CONFIRMED", "CANCELLED"],
        products: []
    };

    const root = document.querySelector(".layout");
    const defaultDate = root?.dataset?.defaultDate || "";

    const summaryDateInput = document.getElementById("summaryDate");
    const ordersDateInput = document.getElementById("ordersDate");
    summaryDateInput.value = defaultDate;
    ordersDateInput.value = defaultDate;

    document.getElementById("loadSummaryBtn").addEventListener("click", loadSummary);
    document.getElementById("loadOrdersBtn").addEventListener("click", loadOrders);
    document.getElementById("healthBtn").addEventListener("click", loadHealth);
    document.getElementById("refreshAllBtn").addEventListener("click", refreshAll);
    document.getElementById("createProductForm").addEventListener("submit", createProduct);
    document.getElementById("ordersBody").addEventListener("click", onOrdersAction);
    document.getElementById("productsBody").addEventListener("click", onProductsAction);

    refreshAll();

    async function refreshAll() {
        clearError("summaryError");
        clearError("ordersError");
        clearError("productsError");
        await Promise.all([loadSummary(), loadOrders(), loadProducts()]);
    }

    async function loadSummary() {
        const date = encodeURIComponent(summaryDateInput.value);
        const errorId = "summaryError";
        try {
            const summary = await api(`/api/admin/orders/summary?date=${date}`);
            document.getElementById("summaryMeta").textContent = `Delivery date: ${summary.deliveryDate}`;
            document.getElementById("totalOrders").textContent = summary.totalOrders;
            document.getElementById("totalProducts").textContent = summary.productSummary.length;

            const tbody = document.getElementById("summaryProductsBody");
            tbody.innerHTML = "";
            summary.productSummary.forEach((item) => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${escapeHtml(item.productName)}</td>
                    <td>${item.totalQuantity}</td>
                `;
                tbody.appendChild(tr);
            });
            clearError(errorId);
        } catch (e) {
            setError(errorId, e.message);
        }
    }

    async function loadOrders() {
        const date = encodeURIComponent(ordersDateInput.value);
        const errorId = "ordersError";
        try {
            const orders = await api(`/api/admin/orders?date=${date}`);
            const tbody = document.getElementById("ordersBody");
            tbody.innerHTML = "";

            orders.forEach((order) => {
                const options = state.orderStatuses
                    .map((status) => `<option value="${status}" ${status === order.status ? "selected" : ""}>${status}</option>`)
                    .join("");
                const items = (order.items || [])
                    .map((it) => `${escapeHtml(it.productName)} x${it.quantity}`)
                    .join(", ");

                const tr = document.createElement("tr");
                tr.dataset.orderId = order.id;
                tr.innerHTML = `
                    <td>${order.id}</td>
                    <td>${escapeHtml(order.customerName || "")}</td>
                    <td>${escapeHtml(order.customerPhone || "")}</td>
                    <td>
                        <select class="order-status">${options}</select>
                    </td>
                    <td>${escapeHtml(items)}</td>
                    <td>${order.totalPrice}</td>
                    <td><button type="button" class="button small" data-action="update-order-status">Update</button></td>
                `;
                tbody.appendChild(tr);
            });
            clearError(errorId);
        } catch (e) {
            setError(errorId, e.message);
        }
    }

    async function onOrdersAction(event) {
        const action = event.target?.dataset?.action;
        if (action !== "update-order-status") {
            return;
        }
        const row = event.target.closest("tr");
        const orderId = row?.dataset?.orderId;
        const status = row?.querySelector(".order-status")?.value;
        if (!orderId || !status) {
            return;
        }

        try {
            await api(`/api/admin/orders/${orderId}/status`, {
                method: "PUT",
                body: JSON.stringify({status})
            });
            clearError("ordersError");
        } catch (e) {
            setError("ordersError", e.message);
        }
    }

    async function loadProducts() {
        try {
            const products = await api("/api/products");
            state.products = products;

            const tbody = document.getElementById("productsBody");
            tbody.innerHTML = "";

            products.forEach((product) => {
                const tr = document.createElement("tr");
                tr.dataset.productId = product.id;
                tr.innerHTML = `
                    <td>${product.id}</td>
                    <td><input class="p-name" value="${escapeAttr(product.name)}" maxlength="255"></td>
                    <td><input class="p-description" value="${escapeAttr(product.description || "")}" maxlength="2000"></td>
                    <td><input class="p-price" type="number" min="1" value="${product.price}"></td>
                    <td><input class="p-unit" value="${escapeAttr(product.unit)}" maxlength="50"></td>
                    <td><input class="p-sort-order" type="number" value="${product.sortOrder}"></td>
                    <td>
                        <button type="button" class="button small" data-action="update-product">Update</button>
                        <button type="button" class="button small danger" data-action="deactivate-product">Deactivate</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
            clearError("productsError");
        } catch (e) {
            setError("productsError", e.message);
        }
    }

    async function createProduct(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const payload = {
            name: form.name.value.trim(),
            description: form.description.value.trim(),
            price: Number(form.price.value),
            unit: form.unit.value.trim(),
            active: true,
            sortOrder: Number(form.sortOrder.value)
        };

        try {
            await api("/api/admin/products", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            form.reset();
            form.sortOrder.value = 0;
            await loadProducts();
            clearError("productsError");
        } catch (e) {
            setError("productsError", e.message);
        }
    }

    async function onProductsAction(event) {
        const action = event.target?.dataset?.action;
        if (!action) {
            return;
        }
        const row = event.target.closest("tr");
        const productId = row?.dataset?.productId;
        if (!productId) {
            return;
        }

        try {
            if (action === "update-product") {
                const payload = {
                    name: row.querySelector(".p-name").value.trim(),
                    description: row.querySelector(".p-description").value.trim(),
                    price: Number(row.querySelector(".p-price").value),
                    unit: row.querySelector(".p-unit").value.trim(),
                    active: true,
                    sortOrder: Number(row.querySelector(".p-sort-order").value)
                };
                await api(`/api/admin/products/${productId}`, {
                    method: "PUT",
                    body: JSON.stringify(payload)
                });
            }

            if (action === "deactivate-product") {
                await api(`/api/admin/products/${productId}`, {
                    method: "DELETE"
                });
            }

            await loadProducts();
            clearError("productsError");
        } catch (e) {
            setError("productsError", e.message);
        }
    }

    async function loadHealth() {
        const box = document.getElementById("healthBox");
        try {
            const payload = await api("/api/health");
            box.textContent = JSON.stringify(payload, null, 2);
        } catch (e) {
            box.textContent = `Error: ${e.message}`;
        }
    }

    async function api(url, options) {
        const response = await fetch(url, {
            headers: {"Content-Type": "application/json"},
            ...options
        });

        let body = null;
        try {
            body = await response.json();
        } catch (_ignored) {
            body = null;
        }

        if (!response.ok) {
            const message = body?.message || `Request failed (${response.status})`;
            throw new Error(message);
        }
        return body;
    }

    function setError(id, message) {
        document.getElementById(id).textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).textContent = "";
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    function escapeAttr(value) {
        return escapeHtml(value);
    }
})();
