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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class SpringAIDeepSeekService implements AiService {
    private static final int SIMULATED_USER_COUNT = 100;
    private static final List<String> SENTIMENT_DIRECTIONS = List.of(
            "positive: show genuine satisfaction, enthusiasm, relief or optimism",
            "negative: show believable frustration, disappointment, concern or criticism",
            "neutral: share a matter-of-fact observation or question without strong emotion",
            "mixed: combine appreciation with a reservation, doubt or minor frustration"
    );
    private static final List<String> WRITING_STYLES = List.of(
            "concise and casual",
            "reflective and personal",
            "informal with light humor",
            "direct and slightly sarcastic",
            "practical and technical, without sounding like documentation"
    );

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
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long tweetId = random.nextLong(1, Long.MAX_VALUE);
        int userId = random.nextInt(1, SIMULATED_USER_COUNT + 1);
        String writingStyle = WRITING_STYLES.get((userId - 1) % WRITING_STYLES.size());
        String sentimentDirection = SENTIMENT_DIRECTIONS.get(random.nextInt(SENTIMENT_DIRECTIONS.size()));

        Prompt prompt = promptTemplate.create(Map.of(
                "keywords",
                String.join(", ", configData.getStreamingDataKeywords()),

                "currentTime",
                ZonedDateTime.now(ZoneOffset.UTC).toString(),

                "tweetId",
                tweetId,

                "userId",
                userId,

                "writingStyle",
                writingStyle,

                "sentimentDirection",
                sentimentDirection,

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
