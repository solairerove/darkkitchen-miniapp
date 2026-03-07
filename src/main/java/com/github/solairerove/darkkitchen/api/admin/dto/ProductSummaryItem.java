package com.github.solairerove.darkkitchen.api.admin.dto;

public record ProductSummaryItem(
        Long productId,
        String productName,
        int totalQuantity
) {
}
