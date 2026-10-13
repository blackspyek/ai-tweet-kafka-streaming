package com.microservices.demo.elastic.query.service.impl;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.QueryType;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.error.handler.ElasticQueryServiceException;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceWordCountResponseModel;
import com.microservices.demo.elastic.query.model.assembler.ElasticQueryServiceResponseModelAssembler;
import com.microservices.demo.elastic.query.service.ElasticQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TwitterElasticQueryService implements ElasticQueryService {
    private final ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler;
    private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;
    private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;
    private final WebClient.Builder webClientBuilder;
    public TwitterElasticQueryService(ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler, ElasticQueryClient<TwitterIndexModel> elasticQueryClient, ElasticQueryServiceConfigData elasticQueryServiceConfigData, @Qualifier("webClientBuilder") WebClient.Builder webClientBuilder) {
        this.elasticQueryServiceResponseModelAssembler = elasticQueryServiceResponseModelAssembler;
        this.elasticQueryClient = elasticQueryClient;
        this.elasticQueryServiceConfigData = elasticQueryServiceConfigData;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        return elasticQueryServiceResponseModelAssembler.toModel(elasticQueryClient.getIndexModelById(id));
    }

    @Override
    public ElasticQueryServiceAnalyticsResponseModel getDocumentsByText(String text, String accessToken) {
        List<ElasticQueryServiceResponseModel> models = elasticQueryServiceResponseModelAssembler.toModels(
                elasticQueryClient.getIndexModelByText(text)
        );
        return ElasticQueryServiceAnalyticsResponseModel.builder()
                .queryResponseModels(models)
                .wordCount(getWordCount(text, accessToken))
                .build();
    }

    private Long getWordCount(String text, String accessToken) {
        if (QueryType.KAFKA_STATE_STORE.getType().equals(elasticQueryServiceConfigData.getWebClient().getQueryType())){
            return getWordCountFromKafkaStateStore(text, accessToken).getWordCount();
        }
        return 0L;
    }

    private ElasticQueryServiceWordCountResponseModel getWordCountFromKafkaStateStore(String text, String accessToken) {
        ElasticQueryServiceConfigData.Query queryFromKafkaStateStore = elasticQueryServiceConfigData.getQueryFromKafkaStateStore();
        return retrieveResponseModel(text, accessToken, queryFromKafkaStateStore);
    }

    private ElasticQueryServiceWordCountResponseModel retrieveResponseModel(
            String text,
            String accessToken,
            ElasticQueryServiceConfigData.Query queryFromKafkaStateStore
    ) {
        return webClientBuilder
                .build()
                .method(HttpMethod.valueOf(queryFromKafkaStateStore.getMethod()))
                .uri(
                        queryFromKafkaStateStore.getUri(),
                        uriBuilder -> uriBuilder.build(text)
                )
                .headers(headers -> headers.setBearerAuth(accessToken))
                .accept(MediaType.valueOf(queryFromKafkaStateStore.getAccept()))
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        response -> Mono.just(
                                new BadCredentialsException("Not authenticated!")
                        )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> Mono.just(
                                new ElasticQueryServiceException(
                                        response.statusCode().toString()
                                )

                        )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.just(
                                new IllegalStateException(
                                        response.statusCode().toString()
                                )
                        )
                )
                .bodyToMono(ElasticQueryServiceWordCountResponseModel.class)
                .log()
                .block();
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        return elasticQueryServiceResponseModelAssembler.toModels(
                elasticQueryClient.getAllIndexModels()
        );
    }
}
