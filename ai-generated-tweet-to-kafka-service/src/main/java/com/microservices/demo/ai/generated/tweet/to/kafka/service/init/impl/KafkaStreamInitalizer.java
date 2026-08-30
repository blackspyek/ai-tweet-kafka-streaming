package com.microservices.demo.ai.generated.tweet.to.kafka.service.init.impl;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaStreamInitalizer implements StreamInitializer {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaStreamInitalizer(KafkaConfigData kafkaConfigData, KafkaAdminClient kafkaAdminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    @Override
    public boolean init() {
        try {
            kafkaAdminClient.createTopics();
            kafkaAdminClient.checkSchemaRegistry();
            kafkaAdminClient.checkTopicsCreated();
            log.info("Topics with name {} is ready for  operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
