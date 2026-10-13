package com.microservices.demo.kafka.streams.service.security;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.config.KafkaStreamsServiceConfigData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Qualifier("elastic-query-service-audience-validator")
@Component
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final KafkaStreamsServiceConfigData kafkaStreamsServiceConfigData;

    public AudienceValidator(KafkaStreamsServiceConfigData kafkaStreamsServiceConfigData) {
        this.kafkaStreamsServiceConfigData = kafkaStreamsServiceConfigData;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (Objects.requireNonNull(token.getAudience()).contains(
                kafkaStreamsServiceConfigData.getCustomAudience()
        )){
            return OAuth2TokenValidatorResult.success();
        } else {
            OAuth2Error audienceError = new OAuth2Error("invalid_token", "The required audience: " + kafkaStreamsServiceConfigData.getCustomAudience() + " is missing!", null);
            return OAuth2TokenValidatorResult.failure(audienceError);
        }

    }
}

