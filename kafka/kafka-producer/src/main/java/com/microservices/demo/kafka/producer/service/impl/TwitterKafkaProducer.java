package com.microservices.demo.kafka.producer.service.impl;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Service
@Slf4j
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {

    private KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    public TwitterKafkaProducer(KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        log.info("Sending message {} to topic {}", message, topicName);
        CompletableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);
        kafkaResultFuture.whenComplete(addCallback(topicName, key));
    }

    @PreDestroy
    public void close(){
        if (kafkaTemplate != null) {
            log.info("Closing Kafka Producer");
            kafkaTemplate.destroy();
        }
    }

    private static BiConsumer<SendResult<Long, TwitterAvroModel>, Throwable> addCallback(String topicName, Long key) {
        return (sendResult, throwable) -> {
            if (throwable != null) {
                log.error(
                        "Error while sending message with key {} to Kafka topic {}",
                        key,
                        topicName,
                        throwable
                );
            } else {
                RecordMetadata metadata = sendResult.getRecordMetadata();

                log.info(
                        "Message sent successfully: topic={}, partition={}, offset={}, key={}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        key
                );
            }
        };
    }
}
