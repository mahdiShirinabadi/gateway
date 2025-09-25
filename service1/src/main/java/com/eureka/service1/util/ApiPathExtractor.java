package com.eureka.service1.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting clean API paths from HttpServletRequest
 */
@Component
@Log4j2
public class ApiPathExtractor {

    /**
     * Extract clean API path from HttpServletRequest
     * Removes context path and servlet path to get the actual API endpoint
     */
    public String extractCleanApiPath(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI();
            String contextPath = request.getContextPath();
            String servletPath = request.getServletPath();
            
            log.debug("Original request path: {}", requestPath);
            log.debug("Context path: {}", contextPath);
            log.debug("Servlet path: {}", servletPath);
            
            // Start with the full request path
            String cleanApiPath = requestPath;
            
            // Remove context path if it exists
            if (contextPath != null && !contextPath.isEmpty() && cleanApiPath.startsWith(contextPath)) {
                cleanApiPath = cleanApiPath.substring(contextPath.length());
                log.debug("After removing context path: {}", cleanApiPath);
            }
            
            // Remove servlet path if it exists
            if (servletPath != null && !servletPath.isEmpty() && cleanApiPath.startsWith(servletPath)) {
                cleanApiPath = cleanApiPath.substring(servletPath.length());
                log.debug("After removing servlet path: {}", cleanApiPath);
            }
            
            // Ensure the path starts with /
            if (!cleanApiPath.startsWith("/")) {
                cleanApiPath = "/" + cleanApiPath;
            }
            
            // If the path is empty or just "/", return "/"
            if (cleanApiPath.isEmpty() || cleanApiPath.equals("/")) {
                cleanApiPath = "/";
            }
            
            log.info("Extracted clean API path: {} from request: {}", cleanApiPath, requestPath);
            return cleanApiPath;
            
        } catch (Exception e) {
            log.error("Error extracting API path: {}", e.getMessage());
            return "/";
        }
    }

    /**
     * Extract API path with method information
     */
    public String extractApiPathWithMethod(HttpServletRequest request) {
        String cleanPath = extractCleanApiPath(request);
        String method = request.getMethod();
        return method + " " + cleanPath;
    }

    /**
     * Check if the API path matches a pattern
     */
    public boolean matchesPattern(String apiPath, String pattern) {
        if (apiPath == null || pattern == null) {
            return false;
        }
        
        // Simple pattern matching - can be enhanced with regex
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return apiPath.matches(regex);
        }
        
        return apiPath.equals(pattern);
    }
}
