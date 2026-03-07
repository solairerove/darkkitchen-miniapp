package com.github.solairerove.darkkitchen.api.order;

import com.github.solairerove.darkkitchen.api.order.dto.CreateOrderRequest;
import com.github.solairerove.darkkitchen.api.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String HEADER_TELEGRAM_USER_ID = "X-Telegram-User-Id";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(@RequestHeader(HEADER_TELEGRAM_USER_ID) Long telegramUserId,
                                     @RequestBody @Valid CreateOrderRequest request) {
        return orderService.createOrder(telegramUserId, request);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@RequestHeader(HEADER_TELEGRAM_USER_ID) Long telegramUserId) {
        return orderService.getMyOrders(telegramUserId);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@RequestHeader(HEADER_TELEGRAM_USER_ID) Long telegramUserId,
                                  @PathVariable Long id) {
        return orderService.getOrder(telegramUserId, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@RequestHeader(HEADER_TELEGRAM_USER_ID) Long telegramUserId,
                                     @PathVariable Long id) {
        return orderService.cancelOrder(telegramUserId, id);
    }
}
