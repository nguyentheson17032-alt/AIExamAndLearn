package com.aiexam.warehouse.elo;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.elo")
public record EloProperties(
        @Min(100) int defaultRating,
        @Min(1) int defaultKFactor,
        @Min(1) int questionKFactor
) {}
