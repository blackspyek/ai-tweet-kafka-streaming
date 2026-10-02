package com.microservices.demo.elastic.query.dataaccess.repository;

import com.microservices.demo.elastic.query.dataaccess.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPermissionRepository
        extends JpaRepository<UserPermission, UUID> {

    @Query(
            nativeQuery = true,
            value = """
            SELECT
                p.user_permission_id AS id,
                u.username,
                d.document_id,
                p.permission_type
            FROM users u
            JOIN user_permissions p ON u.id = p.user_id
            JOIN documents d ON d.id = p.document_id
            WHERE u.username = :username
            """
    )
    Optional<List<UserPermission>> findPermissionsByUsername(
            @Param("username") String username
    );
}
