package com.github.solairerove.darkkitchen.api.telegram;

import com.github.solairerove.darkkitchen.api.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TelegramNotifier {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private final RestClient restClient;
    private final AppProperties appProperties;

    public TelegramNotifier(RestClient.Builder restClientBuilder, AppProperties appProperties) {
        this.restClient = restClientBuilder.baseUrl("https://api.telegram.org").build();
        this.appProperties = appProperties;
    }

    public void sendMessage(String chatId, String text) {
        if (blank(appProperties.getTelegram().getBotToken()) || blank(chatId)) {
            log.warn("Telegram credentials are not configured; skipping message send.");
            return;
        }

        String path = "/bot" + appProperties.getTelegram().getBotToken() + "/sendMessage";
        restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "chat_id", chatId,
                        "text", text,
                        "parse_mode", "HTML"
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
