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

import java.util.Map;

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
    public String generateTweet() throws AiGeneratedTweetToKafkaServiceException {
        BeanOutputConverter<TweetResponse> converter = new BeanOutputConverter<>(TweetResponse.class);
        log.info("Converter format: {}", converter.getFormat());

        PromptTemplate promptTemplate = new PromptTemplate(tweetPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                configData.getKeywordsPlaceholder().replace("{", "").replace("}", ""),
                String.join(",", configData.getStreamingDataKeywords()),
                "format",
                converter.getFormat()
        ));

        ChatClientResponse chatClientRespones = chatClient.prompt(prompt)
                .call()
                .chatClientResponse();

        String modelResult = chatClientRespones.chatResponse().getResult().getOutput().getText();


        log.info("Model result from deepseek: {} with model: {}", modelResult,
                chatClientRespones.chatResponse().getMetadata().getModel());
        return modelResult.replaceAll(DEEP_SEEK_THINK_REGEX, "");
    }
}
