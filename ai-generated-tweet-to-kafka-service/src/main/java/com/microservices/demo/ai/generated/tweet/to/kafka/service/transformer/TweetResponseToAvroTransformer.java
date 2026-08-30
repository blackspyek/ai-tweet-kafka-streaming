package com.microservices.demo.ai.generated.tweet.to.kafka.service.transformer;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import org.springframework.stereotype.Component;

@Component
public class TweetResponseToAvroTransformer {
    public TwitterAvroModel getTweetAvroModelFromResponse(TweetResponse tweetResponse) {
        return TwitterAvroModel.newBuilder()
                .setId(tweetResponse.id())
                .setUserId(tweetResponse.user().id())
                .setText(tweetResponse.text())
                .setCreatedAt(tweetResponse.createdAt().toInstant())
                .build();
    }
}
