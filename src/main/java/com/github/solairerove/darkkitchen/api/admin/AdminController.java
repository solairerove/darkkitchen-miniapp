package com.github.solairerove.darkkitchen.api.admin;

import com.github.solairerove.darkkitchen.api.admin.dto.DailySummaryResponse;
import com.github.solairerove.darkkitchen.api.admin.dto.UpdateOrderStatusRequest;
import com.github.solairerove.darkkitchen.api.order.Order;
import com.github.solairerove.darkkitchen.api.order.OrderService;
import com.github.solairerove.darkkitchen.api.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    @GetMapping("/summary")
    public DailySummaryResponse summary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return adminService.getSummary(date);
    }

    @GetMapping
    public List<OrderResponse> orders(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return adminService.getOrders(date).stream().map(orderService::toResponse).toList();
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateOrderStatusRequest request) {
        Order updated = adminService.setOrderStatus(id, request.status());
        return orderService.toResponse(updated);
    }
}
