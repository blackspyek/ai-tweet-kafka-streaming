package com.microservices.demo.kafka.admin.client;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.RetryConfigData;
import com.microservices.demo.kafka.admin.exception.KafkaClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class KafkaAdminClient {
    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;

    public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData, AdminClient adminClient, RetryTemplate retryTemplate, WebClient webClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.retryConfigData = retryConfigData;
        this.adminClient = adminClient;
        this.retryTemplate = retryTemplate;
        this.webClient = webClient;
    }

    public void createTopics(){
        AtomicInteger attempt = new AtomicInteger();
        try{
            retryTemplate.execute(
                    () -> doCreateTopics(attempt.incrementAndGet())
            );
        } catch (Exception e){
            throw new KafkaClientException("Reached max number of retry for creating kafka topic(s)");
        }
    }
    public void checkTopicsCreated() {
        Collection<TopicListing> topics = getTopics();
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        for (String topic : kafkaConfigData.getTopicNamesToCreate()){
            while (!isTopicCreated(topics, topic)) {
                checkMaxRetry(retryCount++, maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
                topics = getTopics();
            }
        }
    }
    public void checkSchemaRegistry(){
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        while (!getSchemaRegistryStatus().is2xxSuccessful()) {
            checkMaxRetry(retryCount++, maxRetry);
            sleep(sleepTimeMs);
            sleepTimeMs *= multiplier;
        }
    }
    private HttpStatusCode getSchemaRegistryStatus(){
        try {
            ResponseEntity<Void> response = webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return response != null
                    ? response.getStatusCode()
                    : HttpStatus.SERVICE_UNAVAILABLE;
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    private void sleep(Long sleepTimeMs) {
        try{
            Thread.sleep(sleepTimeMs);
        }  catch (InterruptedException e){
            throw new KafkaClientException("Error  while sleeping for waiting new created topics!");
        }
    }

    private void checkMaxRetry(int retry, Integer maxRetry) {
        if (retry >= maxRetry) {
            throw new KafkaClientException("Max retry exceeded");
        }
    }

    private boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
        if (topics == null) {
            return false;
        }
        return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
    }

    private CreateTopicsResult doCreateTopics(int attempt){
        List<String> topicNames = kafkaConfigData.getTopicNamesToCreate();
        log.info("Creating {} topic(s), a ttempt {}", topicNames.size(), attempt);
        List<NewTopic> kafkaTopics = topicNames.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.getNumOfPartitions(),
                kafkaConfigData.getReplicationFactor()
        )).toList();

        return adminClient.createTopics(kafkaTopics);
    }
    private Collection<TopicListing> getTopics() {
        AtomicInteger attempt = new AtomicInteger();
        Collection<TopicListing> result;
        try{
            result = retryTemplate.execute(
                    () -> doGetTopics(attempt.incrementAndGet())
            );
        } catch (Throwable e){
            throw new KafkaClientException("Reached max number of retry for creating kafka topic(s)", e);
        }
        return result;
    }

    private Collection<TopicListing> doGetTopics(int attempt) throws ExecutionException, InterruptedException {
        log.info(
                "Reading kafka topic {}, attempt {}", kafkaConfigData.getTopicName(), attempt
        );
        Collection<TopicListing> topics = adminClient.listTopics().listings().get();
        if (topics != null && !topics.isEmpty()) {
            topics.forEach(topic -> log.debug("Found topic {}", topic));
        }
        return topics;
    }
}
