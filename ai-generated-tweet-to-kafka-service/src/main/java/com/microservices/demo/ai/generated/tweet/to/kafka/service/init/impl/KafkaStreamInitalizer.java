package com.microservices.demo.ai.generated.tweet.to.kafka.service.init.impl;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamInitalizer implements StreamInitializer {
    @Override
    public boolean init() {
        return true;
    }
}
