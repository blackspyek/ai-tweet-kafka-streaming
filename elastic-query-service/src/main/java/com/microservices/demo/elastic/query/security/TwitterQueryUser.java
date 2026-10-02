package com.microservices.demo.elastic.query.security;

import com.microservices.demo.elastic.query.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;

@Builder
@Getter
public class TwitterQueryUser implements UserDetails {
    private String username;
    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    private Map<String, PermissionType> permissions;
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
