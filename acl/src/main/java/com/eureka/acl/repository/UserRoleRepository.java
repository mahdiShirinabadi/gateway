package com.eureka.acl.repository;

import com.eureka.acl.entity.Role;
import com.eureka.acl.entity.User;
import com.eureka.acl.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    // Find all roles for a user
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    // Find all roles for a user by username
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.username = :username")
    List<Role> findRolesByUsername(@Param("username") String username);
    
    // Find primary role for a user
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.username = :username AND ur.isPrimary = true")
    Optional<Role> findPrimaryRoleByUsername(@Param("username") String username);
    
    // Find all users with a specific role
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);
    
    // Find all users with a specific role by role name
    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role.name = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);
    
    // Check if user has specific role
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.user.username = :username AND ur.role.name = :roleName")
    boolean userHasRole(@Param("username") String username, @Param("roleName") String roleName);
    
    // Find user-role relationship
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.username = :username AND ur.role.name = :roleName")
    Optional<UserRole> findByUserAndRole(@Param("username") String username, @Param("roleName") String roleName);
    
    // Find all user-role relationships for a user
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.username = :username")
    List<UserRole> findByUsername(@Param("username") String username);
}
