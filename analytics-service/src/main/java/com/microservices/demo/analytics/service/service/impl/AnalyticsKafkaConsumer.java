package com.microservices.demo.analytics.service.service.impl;

import com.microservices.demo.analytics.service.dataaccess.entity.AnalyticsEntity;
import com.microservices.demo.analytics.service.dataaccess.repository.AnalyticsRepository;
import com.microservices.demo.analytics.service.service.KafkaConsumer;
import com.microservices.demo.analytics.service.transformer.AvroToDbEntityModelTransformer;
import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.avro.model.TwitterAnalyticsAvroModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
public class AnalyticsKafkaConsumer implements KafkaConsumer<TwitterAnalyticsAvroModel> {
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaAdminClient kafkaAdminClient;
    private final KafkaConfigData kafkaConfigData;
    private final AvroToDbEntityModelTransformer avroToDbEntityModelTransformer;
    private final AnalyticsRepository analyticsRepository;

    public AnalyticsKafkaConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaAdminClient kafkaAdminClient, KafkaConfigData kafkaConfigData, AvroToDbEntityModelTransformer avroToDbEntityModelTransformer, AnalyticsRepository analyticsRepository) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaAdminClient = kafkaAdminClient;
        this.kafkaConfigData = kafkaConfigData;
        this.avroToDbEntityModelTransformer = avroToDbEntityModelTransformer;
        this.analyticsRepository = analyticsRepository;
    }

    @EventListener
    public void onAppStarted(ApplicationStartedEvent event) {
        kafkaAdminClient.checkTopicsCreated();
        log.info("Kafka consumer started and is ready for operations with topics: {}", kafkaConfigData.getTopicNamesToCreate());
        kafkaListenerEndpointRegistry.getListenerContainer("twitterAnalyticsTopicListener").start();
    }

    @Override
    @KafkaListener(id = "twitterAnalyticsTopicListener", topics = "${kafka-config.topic-name}", autoStartup = "false")
    public void receive(@Payload List<TwitterAnalyticsAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of emessage received with keys {}, partitions {}  and offsets {}," + "sendint it to database: Thread id {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString(), Thread.currentThread().threadId());
        List<AnalyticsEntity> twitterAnalyticsEntities = avroToDbEntityModelTransformer.getEntityModel(messages);
        analyticsRepository.batchPersist(twitterAnalyticsEntities);
        log.info("{} number of messages send to database", twitterAnalyticsEntities.size());

    }
}
