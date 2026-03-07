package com.github.solairerove.darkkitchen.api;

import com.github.solairerove.darkkitchen.api.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class DarkKitchenApplication {

    public static void main(String[] args) {
        SpringApplication.run(DarkKitchenApplication.class, args);
    }
}
