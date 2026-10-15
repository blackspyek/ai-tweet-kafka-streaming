package com.microservices.demo.analytics.service.security;

import com.microservices.demo.analytics.service.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
@Getter
public class AnalyticsUser implements UserDetails {
    private String username;
    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return Constants.NA;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
