package com.eureka.acl.repository;

import com.eureka.acl.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByName(String name);
    
    List<Group> findByIsActiveTrue();
    
    @Query("SELECT g FROM Group g WHERE g.isActive = true")
    List<Group> findActiveGroups();
}
