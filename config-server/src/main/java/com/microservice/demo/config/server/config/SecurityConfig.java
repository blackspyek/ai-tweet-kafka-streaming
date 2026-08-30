package com.microservice.demo.config.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig{
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/encrypt",
                                "/actuator/**",
                                "/encrypt/**",
                                "/decrypt",
                                "/decrypt/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/encrypt",
                                "/encrypt/**",
                                "/decrypt",
                                "/decrypt/**"
                        )
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
