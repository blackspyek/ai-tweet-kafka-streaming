package com.microservices.demo.elastic.query.config;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private final ElasticQueryServiceConfigData configData;

    public WebClientConfig(ElasticQueryServiceConfigData configData) {
        this.configData = configData;
    }

    @LoadBalanced
    @Bean("webClientBuilder")
    WebClient.Builder webClientBuilder() {
        ElasticQueryServiceConfigData.WebClient config =
                configData.getWebClient();

        HttpClient httpClient = HttpClient.create()
                .option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        config.getConnectTimeoutMs()
                )
                .responseTimeout(
                        Duration.ofMillis(config.getReadTimeoutMs())
                )
                .doOnConnected(connection ->
                        connection.addHandlerLast(
                                new WriteTimeoutHandler(
                                        config.getWriteTimeoutMs(),
                                        TimeUnit.MILLISECONDS
                                )
                        )
                );

        return WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(httpClient)
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        config.getContentType()
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        config.getAcceptType()
                )
                .codecs(codecs ->
                        codecs.defaultCodecs()
                                .maxInMemorySize(config.getMaxInMemorySize())
                );
    }
}