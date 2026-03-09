package com.github.solairerove.darkkitchen.api.order;

import com.github.solairerove.darkkitchen.api.order.dto.CreateOrderRequest;
import com.github.solairerove.darkkitchen.api.order.dto.OrderResponse;
import com.github.solairerove.darkkitchen.api.telegram.TelegramPrincipal;
import com.github.solairerove.darkkitchen.api.telegram.TelegramUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@TelegramPrincipal TelegramUser user,
                                                     @RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(user.id(), request));
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@TelegramPrincipal TelegramUser user) {
        return orderService.getMyOrders(user.id());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@TelegramPrincipal TelegramUser user,
                                  @PathVariable Long id) {
        return orderService.getOrder(user.id(), id);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@TelegramPrincipal TelegramUser user,
                                            @PathVariable Long id) {
        orderService.cancelOrder(user.id(), id);
        return ResponseEntity.ok().build();
    }
}
