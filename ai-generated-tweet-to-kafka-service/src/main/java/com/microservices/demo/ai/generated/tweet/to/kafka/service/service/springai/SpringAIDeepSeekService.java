package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AiGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AiService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;
import com.microservices.demo.config.AiGeneratedTweetToKafkaServiceConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class SpringAIDeepSeekService implements AiService {
    private final ChatClient chatClient;
    private final AiGeneratedTweetToKafkaServiceConfigData configData;

    public SpringAIDeepSeekService(ChatClient chatClient, AiGeneratedTweetToKafkaServiceConfigData configData) {
        this.chatClient = chatClient;
        this.configData = configData;
    }

    @Value("classpath:/templates/tweet-prompt.st")
    private Resource tweetPrompt;

    public static final String DEEP_SEEK_THINK_REGEX = "(?s)<think>.*?</think>";

    @Override
    public TweetResponse generateTweet() throws AiGeneratedTweetToKafkaServiceException {
        BeanOutputConverter<TweetResponse> converter = new BeanOutputConverter<>(TweetResponse.class);
        log.info("Converter format: {}", converter.getFormat());

        PromptTemplate promptTemplate = new PromptTemplate(tweetPrompt);
        long tweetId = ThreadLocalRandom.current()
                .nextLong(1, Long.MAX_VALUE);

        long userId = ThreadLocalRandom.current()
                .nextLong(1, Long.MAX_VALUE);

        Prompt prompt = promptTemplate.create(Map.of(
                "keywords",
                String.join(", ", configData.getStreamingDataKeywords()),

                "currentTime",
                ZonedDateTime.now(ZoneOffset.UTC).toString(),

                "tweetId",
                tweetId,

                "userId",
                userId,

                "requestId",
                UUID.randomUUID().toString(),

                "format",
                converter.getFormat()
        ));

        ChatClientResponse chatClientRespones = chatClient.prompt(prompt)
                .call()
                .chatClientResponse();

        String modelResult = chatClientRespones.chatResponse().getResult().getOutput().getText().replaceAll(DEEP_SEEK_THINK_REGEX, "").trim();
        log.info("Model result from deepseek: {} with model: {}", modelResult,
                chatClientRespones.chatResponse().getMetadata().getModel());
        try {
            return converter.convert(modelResult);
        } catch (Exception exception) {
            throw new AiGeneratedTweetToKafkaServiceException(
                    "Could not convert model response to TweetResponse",
                    exception
            );
        }



    }
}
