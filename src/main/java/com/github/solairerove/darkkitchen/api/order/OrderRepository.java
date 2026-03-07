package com.github.solairerove.darkkitchen.api.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByDeliveryDate(LocalDate date);

    List<Order> findAllByDeliveryDateAndStatus(LocalDate date, OrderStatus status);

    List<Order> findAllByTelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);

    List<Order> findAllByDeliveryDateAndStatusNot(LocalDate date, OrderStatus status);
}
