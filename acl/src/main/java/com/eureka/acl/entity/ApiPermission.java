package com.eureka.acl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Unified entity for API permissions
 * Combines permissions and project_apis tables
 */
@Entity
@Table(name = "api_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiPermission extends Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;                    // permission name (SERVICE1_HELLO_ACCESS)
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;               // project reference
    
    @Column(name = "api_path", nullable = false)
    private String apiPath;                 // API path (/hello)
    
    @Column(name = "http_method", nullable = false)
    private String httpMethod;              // HTTP method (GET, POST)
    
    @Column(nullable = false)
    private String description;             // description
    
    @Column(name = "persian_name", nullable = false)
    private String persianName;             // persian name
    
    @Column(name = "is_critical", nullable = false)
    private boolean isCritical = false;     // is critical permission
    
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;      // is public API
}
