package com.lsiproject.app.propertymanagementmicroservice.configuration;

import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Bean
    feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}