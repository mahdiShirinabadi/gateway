package com.eureka.acl.repository;

import com.eureka.acl.entity.Role;
import com.eureka.acl.entity.RolePermission;
import com.eureka.acl.entity.ApiPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    List<RolePermission> findByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    // Check if role has specific permission
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.role = :role AND rp.permission = :permission")
    boolean existsByRoleAndPermission(@Param("role") Role role, @Param("permission") ApiPermission permission);
    
    // Check if role has permission by role ID and permission ID
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    // Find role permission by role and permission
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role = :role AND rp.permission = :permission")
    Optional<RolePermission> findByRoleAndPermission(@Param("role") Role role, @Param("permission") ApiPermission permission);
    
    // Find permissions by role
    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.role = :role")
    List<ApiPermission> findPermissionsByRole(@Param("role") Role role);
} 