package com.microservices.demo.ai.generated.tweet.to.kafka.service.service;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AiGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;

public interface AiService {
   TweetResponse generateTweet() throws AiGeneratedTweetToKafkaServiceException;
}
