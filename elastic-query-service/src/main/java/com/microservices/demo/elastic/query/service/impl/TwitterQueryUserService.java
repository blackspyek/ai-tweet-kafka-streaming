package com.microservices.demo.elastic.query.service.impl;

import com.microservices.demo.elastic.query.dataaccess.entity.UserPermission;
import com.microservices.demo.elastic.query.dataaccess.repository.UserPermissionRepository;
import com.microservices.demo.elastic.query.service.QueryUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TwitterQueryUserService implements QueryUserService {
    private final UserPermissionRepository userPermissionRepository;

    public TwitterQueryUserService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    @Override
    public Optional<List<UserPermission>> findAllPermissionsByUsername(String username) {
        return userPermissionRepository.findPermissionsByUsername(username);
    }
}
