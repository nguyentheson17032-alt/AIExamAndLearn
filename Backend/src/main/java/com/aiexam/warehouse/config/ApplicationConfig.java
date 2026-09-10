package com.aiexam.warehouse.config;

import com.aiexam.warehouse.ai.AiProperties;
import com.aiexam.warehouse.auth.JwtProperties;
import com.aiexam.warehouse.elo.EloProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AiProperties.class, EloProperties.class})
@EnableRetry
public class ApplicationConfig {
}
