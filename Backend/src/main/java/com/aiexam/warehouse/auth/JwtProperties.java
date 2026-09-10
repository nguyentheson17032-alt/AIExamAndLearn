package com.aiexam.warehouse.auth;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
    public JwtProperties {
        if (accessTokenExpiration == null) {
            accessTokenExpiration = Duration.ofMinutes(15);
        }
        if (refreshTokenExpiration == null) {
            refreshTokenExpiration = Duration.ofDays(7);
        }
    }
}
