package com.microservices.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elastic-query-web-client")
@Data
public class ElasticQueryWebClientConfigData {

    private WebClient webClient = new WebClient();
    private Query queryByText;

    @Data
    public static class WebClient {
        private Integer maxInMemorySize;
        private String baseUrl;
        private String serviceId;
    }

    @Data
    public static class Query{
        private String method;
        private String uri;
    }
}
