package com.github.solairerove.darkkitchen.api.admin.dto;

import com.github.solairerove.darkkitchen.api.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
