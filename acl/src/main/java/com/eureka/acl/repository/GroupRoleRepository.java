package com.eureka.acl.repository;

import com.eureka.acl.entity.Group;
import com.eureka.acl.entity.GroupRole;
import com.eureka.acl.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long> {
    
    Optional<GroupRole> findByGroupAndRole(Group group, Role role);
    
    boolean existsByGroupAndRole(Group group, Role role);
    
    @Query("SELECT gr FROM GroupRole gr WHERE gr.group.id = :groupId")
    List<GroupRole> findByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT gr FROM GroupRole gr WHERE gr.role.id = :roleId")
    List<GroupRole> findByRoleId(@Param("roleId") Long roleId);
}
