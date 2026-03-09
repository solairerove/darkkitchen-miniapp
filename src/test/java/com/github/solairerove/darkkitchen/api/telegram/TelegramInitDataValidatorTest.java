package com.github.solairerove.darkkitchen.api.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.solairerove.darkkitchen.api.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramInitDataValidatorTest {

    private static final String BOT_TOKEN = "123456:TEST_BOT_TOKEN";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String WEB_APP_DATA = "WebAppData";

    private TelegramInitDataValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getTelegram().setBotToken(BOT_TOKEN);
        objectMapper = new ObjectMapper();
        validator = new TelegramInitDataValidator(appProperties, objectMapper);
    }

    @Test
    void validateReturnsTelegramUserForValidInitData() throws Exception {
        String initData = signedInitData();

        TelegramUser user = validator.validate(initData);

        assertThat(user.id()).isEqualTo(123456789L);
        assertThat(user.firstName()).isEqualTo("John");
        assertThat(user.lastName()).isEqualTo("Doe");
        assertThat(user.username()).isEqualTo("johndoe");
    }

    @Test
    void validateThrowsWhenHashIsTampered() throws Exception {
        String initData = signedInitData();
        String tampered = initData.replaceFirst("hash=([0-9a-f])", "hash=0");

        assertThatThrownBy(() -> validator.validate(tampered))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Invalid initData");
    }

    @Test
    void validateThrowsWhenHashIsMissing() throws Exception {
        String initDataWithoutHash = signedInitData().replaceAll("&hash=[^&]+", "");

        assertThatThrownBy(() -> validator.validate(initDataWithoutHash))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Invalid initData");
    }

    private String signedInitData() throws Exception {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", 123456789L);
        user.put("first_name", "John");
        user.put("last_name", "Doe");
        user.put("username", "johndoe");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("query_id", "AAEAAAE");
        params.put("user", objectMapper.writeValueAsString(user));
        params.put("auth_date", "1700000000");

        String dataCheckString = new TreeMap<>(params).entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));

        byte[] secretKey = hmac(WEB_APP_DATA.getBytes(StandardCharsets.UTF_8), BOT_TOKEN.getBytes(StandardCharsets.UTF_8));
        String hash = toHex(hmac(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8)));
        params.put("hash", hash);

        return params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private byte[] hmac(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));
        return mac.doFinal(data);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            sb.append(String.format("%02x", value));
        }
        return sb.toString();
    }
}
