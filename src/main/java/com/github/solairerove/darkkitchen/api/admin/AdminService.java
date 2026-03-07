package com.github.solairerove.darkkitchen.api.admin;

import com.github.solairerove.darkkitchen.api.admin.dto.DailySummaryResponse;
import com.github.solairerove.darkkitchen.api.admin.dto.ProductSummaryItem;
import com.github.solairerove.darkkitchen.api.config.AppProperties;
import com.github.solairerove.darkkitchen.api.order.Order;
import com.github.solairerove.darkkitchen.api.order.OrderService;
import com.github.solairerove.darkkitchen.api.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final OrderService orderService;
    private final AppProperties appProperties;
    private final Clock clock;

    public AdminService(OrderService orderService, AppProperties appProperties) {
        this(orderService, appProperties, Clock.systemUTC());
    }

    AdminService(OrderService orderService, AppProperties appProperties, Clock clock) {
        this.orderService = orderService;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    public DailySummaryResponse getSummary(LocalDate date) {
        LocalDate deliveryDate = date != null ? date : defaultDate();
        List<Order> orders = orderService.getActiveOrdersByDate(deliveryDate);

        Map<Long, ProductSummaryItem> byProduct = new LinkedHashMap<>();
        for (Order order : orders) {
            order.getItems().forEach(item -> {
                Long productId = item.getProduct().getId();
                ProductSummaryItem current = byProduct.get(productId);
                if (current == null) {
                    byProduct.put(productId, new ProductSummaryItem(
                            productId,
                            item.getProduct().getName(),
                            item.getQuantity()
                    ));
                } else {
                    byProduct.put(productId, new ProductSummaryItem(
                            current.productId(),
                            current.productName(),
                            current.totalQuantity() + item.getQuantity()
                    ));
                }
            });
        }

        List<ProductSummaryItem> productSummary = byProduct.values().stream()
                .sorted(Comparator.comparing(ProductSummaryItem::productName))
                .toList();

        List<DailySummaryResponse.OrderSummaryItem> orderItems = orders.stream()
                .map(order -> new DailySummaryResponse.OrderSummaryItem(
                        order.getId(),
                        order.getCustomerName(),
                        order.getCustomerPhone(),
                        order.getStatus(),
                        order.getNote(),
                        order.getItems().stream()
                                .map(item -> new DailySummaryResponse.OrderProductItem(
                                        item.getProduct().getName(),
                                        item.getQuantity()
                                ))
                                .toList()
                ))
                .toList();

        return new DailySummaryResponse(deliveryDate, orders.size(), productSummary, orderItems);
    }

    public List<Order> getOrders(LocalDate date) {
        LocalDate deliveryDate = date != null ? date : defaultDate();
        return orderService.getOrdersByDate(deliveryDate);
    }

    public Order setOrderStatus(Long orderId, OrderStatus status) {
        return orderService.setStatus(orderId, status);
    }

    public LocalDate defaultDate() {
        return LocalDate.now(clock.withZone(ZoneId.of(appProperties.getTimezone()))).plusDays(1);
    }
}
