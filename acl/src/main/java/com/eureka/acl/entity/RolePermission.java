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

@Entity
@Table(name = "role_permissions")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private ApiPermission permission;
    
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