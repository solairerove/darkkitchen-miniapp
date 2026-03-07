package com.github.solairerove.darkkitchen.api.order;

import com.github.solairerove.darkkitchen.api.common.exception.BadRequestException;
import com.github.solairerove.darkkitchen.api.common.exception.ForbiddenException;
import com.github.solairerove.darkkitchen.api.common.exception.NotFoundException;
import com.github.solairerove.darkkitchen.api.config.AppProperties;
import com.github.solairerove.darkkitchen.api.order.dto.CreateOrderRequest;
import com.github.solairerove.darkkitchen.api.order.dto.OrderItemRequest;
import com.github.solairerove.darkkitchen.api.order.dto.OrderResponse;
import com.github.solairerove.darkkitchen.api.product.Product;
import com.github.solairerove.darkkitchen.api.product.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AppProperties appProperties;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository,
                        ProductService productService,
                        AppProperties appProperties,
                        Clock clock) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    public OrderResponse createOrder(Long telegramUserId, CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("Order items must not be empty");
        }

        Order order = new Order();
        order.setTelegramUserId(telegramUserId);
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setNote(request.note());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryDate(calculateDeliveryDate());

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.getActiveProduct(itemRequest.productId());
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long telegramUserId) {
        return orderRepository.findAllByTelegramUserIdOrderByCreatedAtDesc(telegramUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long telegramUserId, Long orderId) {
        Order order = findOwnedOrder(telegramUserId, orderId);
        return toResponse(order);
    }

    public OrderResponse cancelOrder(Long telegramUserId, Long orderId) {
        Order order = findOwnedOrder(telegramUserId, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    public Order setStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByDate(LocalDate date) {
        return orderRepository.findAllByDeliveryDate(date);
    }

    @Transactional(readOnly = true)
    public List<Order> getActiveOrdersByDate(LocalDate date) {
        return orderRepository.findAllByDeliveryDateAndStatusNot(date, OrderStatus.CANCELLED);
    }

    LocalDate calculateDeliveryDate() {
        ZoneId zoneId = ZoneId.of(appProperties.getTimezone());
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);
        LocalDateTime cutoff = now.toLocalDate().atTime(
                appProperties.getOrder().getCutoffHour(),
                appProperties.getOrder().getCutoffMinute()
        );
        return now.toLocalDate().plusDays(now.toLocalDateTime().isBefore(cutoff) ? 1 : 2);
    }

    private Order findOwnedOrder(Long telegramUserId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getTelegramUserId().equals(telegramUserId)) {
            throw new ForbiddenException("Not your order");
        }
        return order;
    }

    public OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemView> itemViews = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemView(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice().longValue()
                ))
                .toList();

        long total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .mapToLong(BigDecimal::longValue)
                .sum();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryDate(),
                order.getStatus(),
                order.getNote(),
                itemViews,
                total,
                order.getCreatedAt()
        );
    }
}
