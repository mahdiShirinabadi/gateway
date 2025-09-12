package com.eureka.acl.repository;

import com.eureka.acl.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    List<RolePermission> findByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
} 