package com.eureka.acl.repository;

import com.eureka.acl.entity.Group;
import com.eureka.acl.entity.User;
import com.eureka.acl.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    
    Optional<UserGroup> findByUserAndGroup(User user, Group group);
    
    boolean existsByUserAndGroup(User user, Group group);
    
    @Query("SELECT ug FROM UserGroup ug WHERE ug.user.id = :userId")
    List<UserGroup> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ug FROM UserGroup ug WHERE ug.group.id = :groupId")
    List<UserGroup> findByGroupId(@Param("groupId") Long groupId);
}
