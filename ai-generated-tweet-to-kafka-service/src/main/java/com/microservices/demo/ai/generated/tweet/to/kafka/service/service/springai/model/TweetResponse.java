package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record TweetResponse(
        ZonedDateTime createdAt,
        Long id,
        String text,
        User user
) {
}
