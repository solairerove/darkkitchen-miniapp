package com.github.solairerove.darkkitchen.api.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 255) String customerName,
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "must be an international phone number") String customerPhone,
        @Size(max = 2000) String note,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
