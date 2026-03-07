package com.github.solairerove.darkkitchen.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @NotNull @Min(1) Long price,
        @NotBlank @Size(max = 50) String unit,
        Boolean active,
        int sortOrder
) {
}
