package com.github.solairerove.darkkitchen.api.order.dto;

import com.github.solairerove.darkkitchen.api.order.OrderStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerPhone,
        LocalDate deliveryDate,
        OrderStatus status,
        String note,
        List<OrderItemView> items,
        Long totalPrice,
        Instant createdAt
) {
    public record OrderItemView(
            Long productId,
            String productName,
            int quantity,
            Long unitPrice
    ) {
    }
}
