package com.microservices.demo.common.config;

import com.microservices.demo.config.RetryConfigData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.resilience.annotation.EnableResilientMethods;

import java.time.Duration;

@Configuration
@EnableResilientMethods
public class RetryConfig {
    private final RetryConfigData retryConfigData;
    public RetryConfig(RetryConfigData retryConfigData) {
        this.retryConfigData = retryConfigData;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(retryConfigData.getMaxAttempts() - 1)
                .multiplier(retryConfigData.getMultiplier())
                .maxDelay(
                        Duration.ofMillis(retryConfigData.getMaxIntervalMs())
                )
                .delay(
                        Duration.ofMillis(retryConfigData.getInitialIntervalMs())
                )
                .build();
        return new RetryTemplate(retryPolicy);
    }
}
