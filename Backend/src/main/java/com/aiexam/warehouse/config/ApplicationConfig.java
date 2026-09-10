package com.aiexam.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@ConfigurationPropertiesScan("com.aiexam.warehouse")
@EnableRetry
public class ApplicationConfig {
}
