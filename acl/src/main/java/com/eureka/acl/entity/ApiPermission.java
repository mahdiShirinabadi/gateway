package com.eureka.acl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Unified entity for API permissions
 * Combines permissions and project_apis tables
 */
@Entity
@Table(name = "api_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ApiPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;                    // permission name (SERVICE1_HELLO_ACCESS)
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;               // project reference
    
    @Column(nullable = false)
    private String apiPath;                 // API path (/hello)
    
    @Column(nullable = false)
    private String httpMethod;              // HTTP method (GET, POST)
    
    @Column(nullable = false)
    private String description;             // description
    
    @Column(nullable = false)
    private String persianName;             // persian name
    
    @Column(nullable = false)
    private boolean isCritical = false;     // is critical permission
    
    @Column(nullable = false)
    private boolean isPublic = false;      // is public API
    
    // Audit fields
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @CreatedBy
    @Column(name = "create_by", nullable = false, updatable = false)
    private String createBy;
    
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @LastModifiedBy
    @Column(name = "update_by")
    private String updateBy;
    
    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;
    
    @Column(name = "deleted_by")
    private String deletedBy;
}
