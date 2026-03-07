package com.github.solairerove.darkkitchen.api.order;

import com.github.solairerove.darkkitchen.api.config.AppProperties;
import com.github.solairerove.darkkitchen.api.product.ProductService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OrderServiceTest {

    @Test
    void calculateDeliveryDateBeforeCutoffReturnsTomorrow() {
        AppProperties props = properties();
        Clock clock = Clock.fixed(Instant.parse("2026-03-06T10:00:00Z"), ZoneOffset.UTC); // 17:00 ICT
        OrderService service = new OrderService(mock(OrderRepository.class), mock(ProductService.class), props, clock);

        assertThat(service.calculateDeliveryDate()).isEqualTo(java.time.LocalDate.of(2026, 3, 7));
    }

    @Test
    void calculateDeliveryDateAtOrAfterCutoffReturnsDayAfterTomorrow() {
        AppProperties props = properties();
        Clock clock = Clock.fixed(Instant.parse("2026-03-06T11:00:00Z"), ZoneOffset.UTC); // 18:00 ICT
        OrderService service = new OrderService(mock(OrderRepository.class), mock(ProductService.class), props, clock);

        assertThat(service.calculateDeliveryDate()).isEqualTo(java.time.LocalDate.of(2026, 3, 8));
    }

    private AppProperties properties() {
        AppProperties props = new AppProperties();
        props.setTimezone("Asia/Ho_Chi_Minh");
        props.getOrder().setCutoffHour(18);
        props.getOrder().setCutoffMinute(0);
        return props;
    }
}
