package com.microservices.demo.ai.generated.tweet.to.kafka.service.runner;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AiService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.transformer.TweetResponseToAvroTransformer;
import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AiStreamRunner implements Runnable{
    private final AiService aiService;

    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final TweetResponseToAvroTransformer tweetResponseToAvroTransformer;

    public AiStreamRunner(AiService aiService, KafkaConfigData kafkaConfigData, KafkaProducer<Long, TwitterAvroModel> kafkaProducer, TweetResponseToAvroTransformer tweetResponseToAvroTransformer) {
        this.aiService = aiService;
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.tweetResponseToAvroTransformer = tweetResponseToAvroTransformer;
    }

    @Override
    public void run() {
        TweetResponse generatedTweet = aiService.generateTweet();
        log.info("Generated tweet is {} sending it to kafka topic {}", generatedTweet, kafkaConfigData.getTopicName());
        TwitterAvroModel twitterAvroModel = tweetResponseToAvroTransformer.getTweetAvroModelFromResponse(generatedTweet);
        kafkaProducer.send(kafkaConfigData.getTopicName(), twitterAvroModel.getUserId(), twitterAvroModel);

    }
}
