package com.microservices.demo.ai.generated.tweet.to.kafka.service.service;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AiGeneratedTweetToKafkaServiceException;

public interface AiService {
    String generateTweet() throws AiGeneratedTweetToKafkaServiceException;
}
