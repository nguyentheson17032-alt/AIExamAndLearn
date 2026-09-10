package com.aiexam.warehouse.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
@EnableConfigurationProperties(OpenAiClientProperties.class)
public class AiConfig {

    @Bean
    OpenAiChatModel openAiChatModel(OpenAiClientProperties properties) {
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(properties.apiKey())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(properties.model()).build())
                .build();
    }

    @Bean
    ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("You are an exam-warehouse AI that returns strict JSON.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
