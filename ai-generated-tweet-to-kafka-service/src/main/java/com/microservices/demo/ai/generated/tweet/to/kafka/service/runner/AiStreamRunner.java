package com.microservices.demo.ai.generated.tweet.to.kafka.service.runner;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AiStreamRunner implements Runnable{
    private final AiService aiService;

    public AiStreamRunner(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public void run() {
        String generatedTweet = aiService.generateTweet();
        log.info("Generated tweet is {}", generatedTweet);

    }
}
