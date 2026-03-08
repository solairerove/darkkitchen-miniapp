package com.github.solairerove.darkkitchen.api.admin;

import com.github.solairerove.darkkitchen.api.config.AppProperties;
import com.github.solairerove.darkkitchen.api.order.OrderStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.ZoneId;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final AppProperties appProperties;

    public AdminPageController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        ZoneId zoneId = ZoneId.of(appProperties.getTimezone());
        model.addAttribute("defaultDate", LocalDate.now(zoneId).plusDays(1));
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/dashboard";
    }
}
