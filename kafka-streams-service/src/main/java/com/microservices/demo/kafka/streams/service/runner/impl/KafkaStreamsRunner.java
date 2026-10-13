package com.microservices.demo.kafka.streams.service.runner.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaStreamsConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.streams.service.runner.StreamsRunner;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {

    private static final String REGEX = "\\W+";
    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final KafkaConfigData kafkaConfigData;
    private final Properties streamsConfiguration;
    private KafkaStreams kafkaStreams;
    private volatile ReadOnlyKeyValueStore<String, Long> keyValueStore;

    public KafkaStreamsRunner(KafkaStreamsConfigData kafkaStreamsConfigData, KafkaConfigData kafkaConfigData, @Qualifier("streamConfiguration") Properties streamsConfiguration) {
        this.kafkaStreamsConfigData = kafkaStreamsConfigData;
        this.kafkaConfigData = kafkaConfigData;
        this.streamsConfiguration = streamsConfiguration;
    }


    @Override
    public void start() {
        final Map<String, String> serdeConfig = Collections.singletonMap(
                kafkaConfigData.getSchemaRegistryUrlKey(),
                kafkaConfigData.getSchemaRegistryUrl()
        );
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<Long, TwitterAvroModel> twitterAvroModelKStream = getTwitterAvroModelKStream(serdeConfig, builder);

        createTopology(twitterAvroModelKStream, serdeConfig);
        startStreaming(builder);

    }
    @PreDestroy
    public void close() {
        if (kafkaStreams != null) {
            kafkaStreams.close();
            log.info("Closing Kafka Streams Streams");
        }
    }

    private void startStreaming(StreamsBuilder builder) {
        final Topology toplogy = builder.build();
        log.info("Topology created: {}", toplogy.describe());
        kafkaStreams = new KafkaStreams(toplogy, streamsConfiguration);
        kafkaStreams.start();
        log.info("Streams started: {}", kafkaStreams.toString());
    }

    private void createTopology(KStream<Long, TwitterAvroModel> twitterAvroModelKStream, Map<String, String> serdeConfig) {
        Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);

        Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = getSerdeAnalyticsModel(serdeConfig);
        twitterAvroModelKStream
                .flatMapValues(value -> Arrays.asList(pattern.split(value.getText().toLowerCase())))
                .groupBy((key, word) -> word)
                .count(Materialized.
                        <String, Long, KeyValueStore<Bytes, byte[]>>as(kafkaStreamsConfigData.getWordCountStoreName()))
                .toStream()
                .map(mapToAnalyticsModel())
                .to(kafkaStreamsConfigData.getOutputTopicName(), Produced.with(Serdes.String(), serdeTwitterAnalyticsAvroModel));
    }

    private KeyValueMapper<
            String,
            Long,
            KeyValue<String, TwitterAnalyticsAvroModel>
            > mapToAnalyticsModel() {

        return (word, count) ->
                new KeyValue<>(
                        word,
                        TwitterAnalyticsAvroModel.newBuilder()
                                .setWord(word)
                                .setWordCount(count)
                                .setCreatedAt(
                                        LocalDateTime.now()
                                                .toEpochSecond(ZoneOffset.UTC)
                                )
                                .build()
                );
    }

    private Serde<TwitterAnalyticsAvroModel> getSerdeAnalyticsModel(Map<String, String> serdeConfig) {
        Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = new SpecificAvroSerde<>();
        serdeTwitterAnalyticsAvroModel.configure(serdeConfig, false);
        return serdeTwitterAnalyticsAvroModel;
    }

    private KStream<Long, TwitterAvroModel> getTwitterAvroModelKStream(Map<String, String> serdeConfig, StreamsBuilder builder) {
        final Serde<TwitterAvroModel> serdeTwitterAvroModel = new SpecificAvroSerde<>();
        serdeTwitterAvroModel.configure(serdeConfig, false);
        return builder.stream(kafkaStreamsConfigData.getInputTopicName(), Consumed.with(Serdes.Long(), serdeTwitterAvroModel));
    }

    @Override
    public Long getValueByKey(String word) {
        if(kafkaStreams != null && kafkaStreams.state() == KafkaStreams.State.RUNNING) {
            if (keyValueStore == null) {
                synchronized (this) {
                    if (keyValueStore == null) {
                        keyValueStore = kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                                kafkaStreamsConfigData.getWordCountStoreName(),
                                QueryableStoreTypes.keyValueStore()
                        ));
                    }
                }
            }
            return keyValueStore.get(word.toLowerCase());
        }
        return 0L;
    }
}
