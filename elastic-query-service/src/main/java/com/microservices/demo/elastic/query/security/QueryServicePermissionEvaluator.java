package com.microservices.demo.elastic.query.security;

import com.microservices.demo.elastic.query.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Component
public class QueryServicePermissionEvaluator implements PermissionEvaluator {
    private static final String SUPER_USER_ROLE = "APP_SUPER_USER_ROLE";
    private final HttpServletRequest httpServletRequest;

    public QueryServicePermissionEvaluator(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public boolean hasPermission(Authentication authentication, @Nullable Object targetDomain, Object permission) {
        if (isSuperUser()) {
            return true;
        }
        if(targetDomain instanceof ElasticQueryServiceRequestModel){
            return preAuthorize(authentication, ((ElasticQueryServiceRequestModel) targetDomain).getId(), permission);
        } else if(targetDomain instanceof ResponseEntity<?> || targetDomain == null) {
            if (targetDomain == null) {
                return true;
            }
            List<ElasticQueryServiceResponseModel> responseBody =
                    ((ResponseEntity<List<ElasticQueryServiceResponseModel>>) targetDomain).getBody();
            Objects.requireNonNull(responseBody);
            return postAuthorize(authentication, responseBody, permission);
        }
        return false;
    }

    private boolean postAuthorize(
            Authentication authentication,
            List<ElasticQueryServiceResponseModel> responseBody,
            Object permission) {

        TwitterQueryUser twitterQueryUser =
                (TwitterQueryUser) authentication.getPrincipal();

        for (ElasticQueryServiceResponseModel responseModel : responseBody) {
            PermissionType userPermission =
                    twitterQueryUser.getPermissions().get(responseModel.getId());

            if (!hasPermission((String) permission, userPermission)) {
                return false;
            }
        }

        return true;
    }

    private boolean preAuthorize(Authentication authentication, String id, Object permission) {
        TwitterQueryUser twitterQueryUser = (TwitterQueryUser) authentication.getPrincipal();
        PermissionType userPermission = twitterQueryUser.getPermissions().get(id);
        return hasPermission((String) permission, userPermission);
    }

    private static boolean hasPermission(Object permission, PermissionType userPermission) {
        return userPermission != null && permission.equals(userPermission.getType());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (targetId == null) {
            return false;
        }
        return preAuthorize(
                authentication,
                (String) targetId,
                permission
        );

    }
    private boolean isSuperUser() {
        return httpServletRequest.isUserInRole(SUPER_USER_ROLE);
    }
}
