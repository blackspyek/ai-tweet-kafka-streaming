package com.microservices.demo.kafka.streams.service.config;

import com.microservices.demo.kafka.streams.service.security.KafkaStreamsUserDetailsService;
import com.microservices.demo.kafka.streams.service.security.KafkaStreamsUserJwtConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final KafkaStreamsUserDetailsService kafkaStreamsUserDetailsService;
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    @Value("${security.paths-to-ignore}")
    private String[] pathsToIgnore;

    public SecurityConfig(KafkaStreamsUserDetailsService kafkaStreamsUserDetailsService, OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        this.kafkaStreamsUserDetailsService = kafkaStreamsUserDetailsService;
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception {
        return http
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                pathsToIgnore
                        )
                        .permitAll()
                        .anyRequest()
                        .fullyAuthenticated()
                )
                .oauth2ResourceServer(
                        oAuth2ResourceServerProperties -> oAuth2ResourceServerProperties
                                .jwt(jwtConfigurer -> jwtConfigurer
                                        .jwtAuthenticationConverter(kafkaStreamsUserJwtAuthConverter()))
                )
                .build();

    }
    @Bean()
    JwtDecoder jwtDecoder(@Qualifier("elastic-query-service-audience-validator") OAuth2TokenValidator<Jwt> jwtValidator, OAuth2TokenValidator<Jwt> audienceValidator) {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(
                oAuth2ResourceServerProperties.getJwt().getIssuerUri());
        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(
                        oAuth2ResourceServerProperties.getJwt().getIssuerUri()
                );
        OAuth2TokenValidator<Jwt> withAudience =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withIssuer);
        return jwtDecoder;
    }
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken>
    kafkaStreamsUserJwtAuthConverter() {
        return new KafkaStreamsUserJwtConverter(kafkaStreamsUserDetailsService);
    }

   }
