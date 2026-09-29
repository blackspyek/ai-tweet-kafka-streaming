package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import com.microservices.demo.config.UserConfigData;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final ElasticQueryWebClientConfigData elasticQueryWebClientConfigData;
    private final UserConfigData userConfigData;

    public WebClientConfig(
            ElasticQueryWebClientConfigData elasticQueryWebClientConfigData,
            UserConfigData userConfigData
    ) {
        this.elasticQueryWebClientConfigData = elasticQueryWebClientConfigData;
        this.userConfigData = userConfigData;
    }

    @LoadBalanced
    @Bean
    WebClient.Builder webClientBuilder() {
        ElasticQueryWebClientConfigData.WebClient config =
                elasticQueryWebClientConfigData.getWebClient();
        return WebClient.builder()
                .filter(ExchangeFilterFunctions.basicAuthentication(userConfigData.getUsername(), userConfigData.getPassword()))
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(config.getMaxInMemorySize()));
    }
}
