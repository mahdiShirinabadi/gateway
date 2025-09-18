package com.eureka.service1.config;

import com.eureka.service1.model.TokenInfo;
import com.eureka.service1.service.TokenInfoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenValidator jwtTokenValidator;
    private final UserDetailsService userDetailsService;
    private final TokenInfoService tokenInfoService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                // Get token info from Redis (token as key, JSON data as value)
                TokenInfo tokenInfo = tokenInfoService.getTokenInfo(token);
                
                if (tokenInfo != null) {
                    // Use cached token info for authentication and authorization
                    String path = request.getRequestURI();
                    String method = request.getMethod();
                    String permissionName = jwtTokenValidator.getPermissionNameForPath(path, method);
                    
                    if (tokenInfo.hasPermission(permissionName)) {
                        // Set Spring Security context from cached token info
                        UserDetails userDetails = userDetailsService.loadUserByUsername(tokenInfo.getUsername());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("CACHE HIT - User authenticated and authorized: {} for path: {} (from Redis JSON data)", 
                                tokenInfo.getUsername(), path);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Access denied - Insufficient permissions");
                        log.warn("CACHE HIT - Authorization failed for user: {} for path: {} with permission: {}", 
                                tokenInfo.getUsername(), path, permissionName);
                        return;
                    }
                } else {
                    // Cache miss - validate with SSO and ACL, then store in Redis
                    if (jwtTokenValidator.validateToken(token)) {
                        String username = jwtTokenValidator.extractUsername(token);
                        if (username != null) {
                            String path = request.getRequestURI();
                            String method = request.getMethod();
                            String permissionName = jwtTokenValidator.getPermissionNameForPath(path, method);
                            
                            if (jwtTokenValidator.checkAuthorization(username, permissionName)) {
                                // Get all permissions for the user and cache them in Redis
                                List<String> allPermissions = getAllUserPermissions(username);
                                TokenInfo newTokenInfo = new TokenInfo(username, allPermissions, "sso");
                                tokenInfoService.storeTokenInfo(token, newTokenInfo);
                                
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.info("CACHE MISS - User authenticated and authorized: {} for path: {} (validated with SSO/ACL and cached)", username, path);
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("Access denied - Insufficient permissions");
                                log.warn("CACHE MISS - Authorization failed for user: {} for path: {} with permission: {}", username, path, permissionName);
                                return;
                            }
                        }
                    } else {
                        log.debug("CACHE MISS - No valid token found in request");
                    }
                }
            } else {
                log.debug("No token found in request");
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private List<String> getAllUserPermissions(String username) {
        try {
            // In a real implementation, you would call the ACL service to get all permissions for the user
            // For now, return a basic set of permissions
            List<String> permissions = new ArrayList<>();
            permissions.add("SERVICE1_ALL_ACCESS");
            permissions.add("SERVICE1_HELLO_ACCESS");
            permissions.add("SERVICE1_ADMIN_ACCESS");
            
            log.debug("Retrieved permissions for user: {} - {}", username, permissions);
            return permissions;
        } catch (Exception e) {
            log.error("Error getting user permissions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
} 