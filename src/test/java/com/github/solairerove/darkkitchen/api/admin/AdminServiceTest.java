package com.github.solairerove.darkkitchen.api.admin;

import com.github.solairerove.darkkitchen.api.admin.dto.DailySummaryResponse;
import com.github.solairerove.darkkitchen.api.config.AppProperties;
import com.github.solairerove.darkkitchen.api.order.Order;
import com.github.solairerove.darkkitchen.api.order.OrderItem;
import com.github.solairerove.darkkitchen.api.order.OrderService;
import com.github.solairerove.darkkitchen.api.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    @Test
    void summaryAggregatesQuantitiesAndUsesDefaultTomorrowDate() {
        OrderService orderService = mock(OrderService.class);
        AppProperties props = new AppProperties();
        props.setTimezone("Asia/Ho_Chi_Minh");
        Clock clock = Clock.fixed(Instant.parse("2026-03-06T05:00:00Z"), ZoneOffset.UTC);

        AdminService service = new AdminService(orderService, props, clock);

        LocalDate defaultDate = LocalDate.of(2026, 3, 7);
        when(orderService.getActiveOrdersByDate(defaultDate)).thenReturn(List.of(
                order(1L, "Syrniki", 2),
                order(2L, "Syrniki", 3)
        ));

        DailySummaryResponse summary = service.getSummary(null);

        assertThat(summary.deliveryDate()).isEqualTo(defaultDate);
        assertThat(summary.totalOrders()).isEqualTo(2);
        assertThat(summary.productSummary()).hasSize(1);
        assertThat(summary.productSummary().get(0).totalQuantity()).isEqualTo(5);
    }

    private Order order(Long id, String productName, int qty) {
        Product product = new Product();
        product.setId(100L);
        product.setName(productName);
        product.setPrice(BigDecimal.valueOf(120000));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPrice(BigDecimal.valueOf(120000));

        Order order = new Order();
        order.setId(id);
        order.setCustomerName("Test");
        order.setDeliveryDate(LocalDate.of(2026, 3, 7));
        order.addItem(item);
        return order;
    }
}
