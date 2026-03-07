package com.github.solairerove.darkkitchen.api.telegram;

import com.github.solairerove.darkkitchen.api.admin.AdminService;
import com.github.solairerove.darkkitchen.api.admin.dto.DailySummaryResponse;
import com.github.solairerove.darkkitchen.api.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DailySummaryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailySummaryScheduler.class);

    private final AdminService adminService;
    private final TelegramNotifier telegramNotifier;
    private final AppProperties appProperties;

    public DailySummaryScheduler(AdminService adminService,
                                 TelegramNotifier telegramNotifier,
                                 AppProperties appProperties) {
        this.adminService = adminService;
        this.telegramNotifier = telegramNotifier;
        this.appProperties = appProperties;
    }

    @Scheduled(cron = "${app.telegram.summary-cron}", zone = "${app.timezone}")
    public void sendDailySummary() {
        DailySummaryResponse summary = adminService.getSummary(null);
        String text = format(summary);
        telegramNotifier.sendMessage(appProperties.getTelegram().getSummaryChatId(), text);
        log.info("Daily summary sent for date {}", summary.deliveryDate());
    }

    String format(DailySummaryResponse summary) {
        String date = summary.deliveryDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        if (summary.totalOrders() == 0) {
            return "No orders for " + date + ".";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Orders for ").append(date).append("\n\n");
        summary.productSummary().forEach(item -> builder
                .append(item.productName())
                .append(" - ")
                .append(item.totalQuantity())
                .append("\n"));
        builder.append("\nTotal orders: ").append(summary.totalOrders());
        return builder.toString();
    }
}
