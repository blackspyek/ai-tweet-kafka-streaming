package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.UserConfigData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class SecurityConfig {
    public static final String GROUPS_CLAIM = "groups";
    public static final String ROLE_PREFIX = "ROLE_";
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.logout-success-url}")
    private String logoutSuccessUrl;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    @Bean
    OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri(logoutSuccessUrl);
        return successHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .anyRequest().fullyAuthenticated()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                );

        return http.build();
    }


    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {

            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {

                if (authority instanceof OidcUserAuthority oidcUserAuthority) {

                    OidcIdToken oidcIdToken =
                            oidcUserAuthority.getIdToken();

                    OidcUserInfo userInfo =
                            oidcUserAuthority.getUserInfo();

                    List<SimpleGrantedAuthority> groupAuthorities =
                            userInfo.getClaimAsStringList(GROUPS_CLAIM)
                                    .stream()
                                    .map(group ->
                                            new SimpleGrantedAuthority(
                                                    ROLE_PREFIX + group.toUpperCase()
                                            )
                                    )
                                    .toList();

                    mappedAuthorities.addAll(groupAuthorities);
                }
            });

            return mappedAuthorities;
        };
    }
}

