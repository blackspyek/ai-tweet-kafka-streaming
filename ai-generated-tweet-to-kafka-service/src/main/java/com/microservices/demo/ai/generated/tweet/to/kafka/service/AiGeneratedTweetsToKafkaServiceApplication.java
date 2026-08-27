package com.microservices.demo.ai.generated.tweet.to.kafka.service;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.runner.AiStreamRunner;
import com.microservices.demo.config.AiGeneratedTweetToKafkaServiceConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@SpringBootApplication
@Slf4j
@EnableScheduling
@ComponentScan(basePackages = "com.microservices.demo")
public class AiGeneratedTweetsToKafkaServiceApplication implements CommandLineRunner {

    private final AiGeneratedTweetToKafkaServiceConfigData aiGeneratedTweetToKafkaServiceConfigData;
    private final StreamInitializer streamInitializer;
    private final AiStreamRunner aiStreamRunner;
    private final TaskScheduler taskScheduler;


    public AiGeneratedTweetsToKafkaServiceApplication(AiGeneratedTweetToKafkaServiceConfigData aiGeneratedTweetToKafkaServiceConfigData, StreamInitializer streamInitializer, AiStreamRunner aiStreamRunner, @Qualifier("taskScheduler") TaskScheduler taskScheduler) {
        this.aiGeneratedTweetToKafkaServiceConfigData = aiGeneratedTweetToKafkaServiceConfigData;
        this.streamInitializer = streamInitializer;
        this.aiStreamRunner = aiStreamRunner;
        this.taskScheduler = taskScheduler;
    }


    static void main(String[] args) {
        SpringApplication.run(AiGeneratedTweetsToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("AiGeneratedTweetsToKafkaServiceApplication started");


        boolean initResult = streamInitializer.init();
        if (initResult) {
            log.info("Starting AI Stream Runner with fixed rate");
            taskScheduler.scheduleAtFixedRate(aiStreamRunner, Duration.of(aiGeneratedTweetToKafkaServiceConfigData.getSchedulerDurationSec(), ChronoUnit.SECONDS));

        } else {
            log.error("Stream initialization failed to initialize the streams");
        }


    }
}
