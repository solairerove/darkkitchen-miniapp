package com.github.solairerove.darkkitchen.api.admin.dto;

import com.github.solairerove.darkkitchen.api.order.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public record DailySummaryResponse(
        LocalDate deliveryDate,
        int totalOrders,
        List<ProductSummaryItem> productSummary,
        List<OrderSummaryItem> orders
) {
    public record OrderSummaryItem(
            Long orderId,
            String customerName,
            String customerPhone,
            OrderStatus status,
            String note,
            List<OrderProductItem> items
    ) {
    }

    public record OrderProductItem(
            String productName,
            int quantity
    ) {
    }
}
