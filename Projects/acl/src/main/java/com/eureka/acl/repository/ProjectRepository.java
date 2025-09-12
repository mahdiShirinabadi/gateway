package com.eureka.acl.repository;

import com.eureka.acl.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
    Optional<Project> findByBaseUrl(String baseUrl);
} 