package com.aiexam.warehouse.ai;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.ai.openai")
public record OpenAiClientProperties(
        @NotBlank String apiKey,
        Chat chat
) {
    public String model() {
        if (chat == null || chat.options() == null || chat.options().model() == null || chat.options().model().isBlank()) {
            return "gpt-4o-mini";
        }
        return chat.options().model();
    }

    public record Chat(Options options) {}

    public record Options(String model) {}
}
