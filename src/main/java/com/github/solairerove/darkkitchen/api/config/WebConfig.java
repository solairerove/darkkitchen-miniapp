package com.github.solairerove.darkkitchen.api.config;

import com.github.solairerove.darkkitchen.api.telegram.TelegramUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TelegramUserArgumentResolver telegramUserArgumentResolver;

    public WebConfig(TelegramUserArgumentResolver telegramUserArgumentResolver) {
        this.telegramUserArgumentResolver = telegramUserArgumentResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("https://web.telegram.org", "null", "https://*.railway.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(telegramUserArgumentResolver);
    }
}
