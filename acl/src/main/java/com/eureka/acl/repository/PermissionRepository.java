package com.eureka.acl.repository;

import com.eureka.acl.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByProjectName(String projectName);
    
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permission.id JOIN Role r ON rp.role.id = r.id JOIN User u ON u.role.id = r.id WHERE u.username = :username")
    List<Permission> findPermissionsByUsername(@Param("username") String username);
} 