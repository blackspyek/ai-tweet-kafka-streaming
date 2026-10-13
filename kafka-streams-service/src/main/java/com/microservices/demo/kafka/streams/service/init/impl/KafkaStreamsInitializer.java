package com.microservices.demo.kafka.streams.service.init.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.streams.service.init.StreamsInitializer;
import lombok.extern.slf4j.Slf4j;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaStreamsInitializer implements StreamsInitializer {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaStreamsInitializer(KafkaConfigData kafkaConfigData, KafkaAdminClient kafkaAdminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    @Override
    public void init() {
        kafkaAdminClient.checkTopicsCreated();
        kafkaAdminClient.checkSchemaRegistry();
        log.info("Topics with name {} is ready for operations!",  kafkaConfigData.getTopicName());
    }
}
