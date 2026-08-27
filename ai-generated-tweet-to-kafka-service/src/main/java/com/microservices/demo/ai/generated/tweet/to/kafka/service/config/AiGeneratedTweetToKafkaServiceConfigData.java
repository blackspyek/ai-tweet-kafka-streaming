package com.microservices.demo.ai.generated.tweet.to.kafka.service.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai-generated-tweet-to-kafka-service")
public class AiGeneratedTweetToKafkaServiceConfigData {
    private List<String> streamingDataKeywords;
    private Long schedulerDurationSec;
    private String prompt;
    private String keywordsPlaceholder;
}
