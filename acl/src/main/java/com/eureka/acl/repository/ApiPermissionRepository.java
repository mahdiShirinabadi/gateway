package com.eureka.acl.repository;

import com.eureka.acl.entity.ApiPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiPermissionRepository extends JpaRepository<ApiPermission, Long> {
    
    // Find by permission name (returns List)
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.name = :name")
    List<ApiPermission> findByName(@Param("name") String name);
    
    // Find single permission by name (returns Optional)
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.name = :name")
    Optional<ApiPermission> findSingleByName(@Param("name") String name);
    
    // Find by project name
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.project.name = :projectName")
    List<ApiPermission> findByProjectName(@Param("projectName") String projectName);
    
    // Find by project and API path
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.project.id = :projectName AND ap.apiPath = :apiPath AND ap.httpMethod = :httpMethod and ap.name=:permissionName")
    Optional<ApiPermission> findByProjectAndApi(@Param("projectName") Long projectId,
                                               @Param("apiPath") String apiPath,
                                               @Param("httpMethod") String httpMethod,
                                                @Param("permissionName") String permissionName);
    
    // Find public APIs
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.isPublic = true")
    List<ApiPermission> findPublicApis();
    
    // Find critical permissions
    @Query("SELECT ap FROM ApiPermission ap WHERE ap.isCritical = true")
    List<ApiPermission> findCriticalPermissions();
}
