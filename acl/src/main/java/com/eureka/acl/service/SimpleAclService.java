package com.eureka.acl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Simple ACL Service
 * Checks user permissions without Redis
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class SimpleAclService {

    /**
     * Check if user has permission to access the requested resource
     */
    public boolean hasPermission(String username, String resource, String action) {
        try {
            log.info("Checking ACL permission for user: {} resource: {} action: {}", username, resource, action);

            // Simple permission logic - in real implementation, this would check database
            // For now, return true for basic permissions
            boolean hasPermission = checkUserPermission(username, resource, action);
            
            log.info("ACL check result: {} for user: {} resource: {} action: {}", 
                    hasPermission, username, resource, action);
            
            return hasPermission;

        } catch (Exception e) {
            log.error("Error checking ACL permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Simple permission checking logic
     * In real implementation, this would query database
     */
    private boolean checkUserPermission(String username, String resource, String action) {
        // Simple logic for demo purposes
        if ("admin".equals(username)) {
            return true; // Admin has all permissions
        }
        
        if ("user".equals(username)) {
            return !resource.contains("admin"); // User can't access admin resources
        }
        
        return false; // Default deny
    }
}
