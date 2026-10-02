package com.microservices.demo.elastic.query.config;

import com.microservices.demo.config.UserConfigData;
import com.microservices.demo.elastic.query.security.AudienceValidator;
import com.microservices.demo.elastic.query.security.QueryServicePermissionEvaluator;
import com.microservices.demo.elastic.query.security.TwitterQueryUserDetailsService;
import com.microservices.demo.elastic.query.security.TwitterQueryUserJwtConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    @Value("${security.paths-to-ignore}")
    private String[] pathsToIgnore;

    public SecurityConfig(TwitterQueryUserDetailsService twitterQueryUserDetailsService, OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        this.twitterQueryUserDetailsService = twitterQueryUserDetailsService;
        this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception {
        return http
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest()
                        .fullyAuthenticated()
                )
                .oauth2ResourceServer(
                        oAuth2ResourceServerProperties -> oAuth2ResourceServerProperties
                                .jwt(jwtConfigurer -> jwtConfigurer
                                        .jwtAuthenticationConverter(TwitterQueryUserJwtConverter()))
                )
                .build();

    }
    @Bean()
    JwtDecoder jwtDecoder(@Qualifier("elastic-query-service-audience-validator") OAuth2TokenValidator<Jwt> jwtValidator, AudienceValidator audienceValidator) {
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
    Converter<Jwt, AbstractAuthenticationToken>
    TwitterQueryUserJwtConverter() {
        return new TwitterQueryUserJwtConverter(twitterQueryUserDetailsService);
    }
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            QueryServicePermissionEvaluator permissionEvaluator
    ) {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();

        expressionHandler.setPermissionEvaluator(permissionEvaluator);

        return expressionHandler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
