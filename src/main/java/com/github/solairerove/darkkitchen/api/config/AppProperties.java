package com.github.solairerove.darkkitchen.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String timezone;

    @NotNull
    private final Order order = new Order();

    @NotNull
    private final Telegram telegram = new Telegram();

    @Getter
    @Setter
    public static class Order {
        private int cutoffHour;
        private int cutoffMinute;
    }

    @Getter
    @Setter
    public static class Telegram {
        private String botToken;
        private String summaryChatId;
        private String summaryCron;
    }
}
