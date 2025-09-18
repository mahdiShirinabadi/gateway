package com.eureka.service1.filter;

import com.eureka.service1.service.TokenValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class Service1SecurityFilter extends OncePerRequestFilter {

    private final TokenValidationService tokenValidationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("=== SERVICE1 SECURITY FILTER ===");
        log.info("Path: {}", path);
        log.info("Method: {}", method);
        log.info("Client IP: {}", getClientIp(request));
        
        // Skip security for health check and public endpoints
        if (isPublicEndpoint(path)) {
            log.info("Public endpoint - skipping security check");
            filterChain.doFilter(request, response);
            return;
        }

        // Check for Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("=== SERVICE1 SECURITY ERROR ===");
            log.warn("No valid Authorization header found");
            log.warn("Error Source: SERVICE1 SECURITY FILTER");
            log.warn("Status: 401 UNAUTHORIZED");
            log.warn("Path: {}", path);
            log.warn("=============================");
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"No valid token provided\"}");
            return;
        }

        String token = authHeader.substring(7);
        
        // Get userId from header (set by Gateway)
        String userId = request.getHeader("X-Authenticated-User");
        if (userId == null) {
            log.warn("=== SERVICE1 SECURITY ERROR ===");
            log.warn("No user ID provided in header");
            log.warn("Error Source: SERVICE1 SECURITY FILTER");
            log.warn("Status: 401 UNAUTHORIZED");
            log.warn("Path: {}", path);
            log.warn("=============================");
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"No user ID provided\"}");
            return;
        }
        
        // Check if token is expired first (from JWT)
        if (tokenValidationService.isTokenExpired(token)) {
            log.warn("=== SERVICE1 SECURITY ERROR ===");
            log.warn("Token has expired");
            log.warn("Error Source: SERVICE1 SECURITY FILTER");
            log.warn("Status: 401 UNAUTHORIZED");
            log.warn("Path: {}", path);
            log.warn("User: {}", userId);
            log.warn("=============================");
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token has expired\"}");
            return;
        }

        // Validate token with signature verification
        if (!tokenValidationService.validateToken(token, userId)) {
            log.warn("=== SERVICE1 SECURITY ERROR ===");
            log.warn("Invalid token signature");
            log.warn("Error Source: SERVICE1 SECURITY FILTER");
            log.warn("Status: 401 UNAUTHORIZED");
            log.warn("Path: {}", path);
            log.warn("User: {}", userId);
            log.warn("=============================");
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid token signature\"}");
            return;
        }

        // Check permissions
        String requiredPermission = getRequiredPermission(path, method);
        if (requiredPermission != null && !tokenValidationService.hasPermission(token, requiredPermission)) {
            log.warn("=== SERVICE1 SECURITY ERROR ===");
            log.warn("Insufficient permissions");
            log.warn("Error Source: SERVICE1 SECURITY FILTER");
            log.warn("Status: 403 FORBIDDEN");
            log.warn("Path: {}", path);
            log.warn("Required Permission: {}", requiredPermission);
            log.warn("=============================");
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Insufficient permissions\"}");
            return;
        }

        // Add user info to request
        String username = tokenValidationService.getUsernameFromToken(token);
        request.setAttribute("authenticatedUser", username);
        request.setAttribute("validatedToken", token);
        
        log.info("=== SERVICE1 SECURITY SUCCESS ===");
        log.info("User: {}", username);
        log.info("Path: {}", path);
        log.info("Permission: {}", requiredPermission);
        log.info("=============================");

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/service1/actuator/health") || 
               path.equals("/service1/actuator/info") ||
               path.startsWith("/service1/public/");
    }

    private String getRequiredPermission(String path, String method) {
        // Convert path to permission format
        String permission = path
                .replace("/service1/", "")
                .replaceAll("/", "_")
                .replaceAll("-", "_")
                .toUpperCase();

        if (permission.startsWith("_")) {
            permission = permission.substring(1);
        }

        return "SERVICE1_" + method + "_" + permission;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
