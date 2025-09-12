package com.eureka.acl.repository;

import com.eureka.acl.entity.ProjectApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectApiRepository extends JpaRepository<ProjectApi, Long> {
    
    @Query("SELECT pa FROM ProjectApi pa WHERE pa.project.name = :projectName")
    List<ProjectApi> findByProjectName(@Param("projectName") String projectName);
    
    @Query("SELECT pa FROM ProjectApi pa WHERE pa.project.name = :projectName AND pa.apiPath = :apiPath AND pa.httpMethod = :httpMethod")
    Optional<ProjectApi> findByProjectNameAndApiPathAndMethod(@Param("projectName") String projectName, 
                                                             @Param("apiPath") String apiPath, 
                                                             @Param("httpMethod") String httpMethod);
    
    @Query("SELECT pa FROM ProjectApi pa WHERE pa.permissionName = :permissionName")
    List<ProjectApi> findByPermissionName(@Param("permissionName") String permissionName);
} 