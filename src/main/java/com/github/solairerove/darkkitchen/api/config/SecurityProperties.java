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
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    @NotNull
    private final Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }
}
