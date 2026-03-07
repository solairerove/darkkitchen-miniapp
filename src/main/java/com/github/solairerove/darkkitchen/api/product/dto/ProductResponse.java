package com.github.solairerove.darkkitchen.api.product.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        String unit,
        boolean active,
        int sortOrder
) {
}
