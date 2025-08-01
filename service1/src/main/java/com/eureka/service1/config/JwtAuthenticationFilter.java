package com.eureka.service1.config;

import com.eureka.service1.model.SignedTokenData;
import com.eureka.service1.service.TokenCacheService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenValidator jwtTokenValidator;
    private final UserDetailsService userDetailsService;
    private final TokenCacheService tokenCacheService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                // Get signed token data with integrity verification
                SignedTokenData signedTokenData = tokenCacheService.getVerifiedTokenData(token);
                
                if (signedTokenData != null) {
                    // Use cached data for auth and authz with signature verification
                    String path = request.getRequestURI();
                    String method = request.getMethod();
                    String permissionName = jwtTokenValidator.getPermissionNameForPath(path, method);
                    
                    if (signedTokenData.hasPermission(permissionName)) {
                        // Set Spring Security context from verified cached data
                        UserDetails userDetails = userDetailsService.loadUserByUsername(signedTokenData.getUsername());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("SIGNED CACHE HIT - User authenticated and authorized: {} for path: {} (signature verified)", 
                                signedTokenData.getUsername(), path);
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Access denied - Insufficient permissions (signed cache)");
                        log.warn("SIGNED CACHE HIT - Authorization failed for user: {} for path: {} with permission: {}", 
                                signedTokenData.getUsername(), path, permissionName);
                        return;
                    }
                } else {
                    // Cache miss or invalid signature - validate with SSO and ACL
                    if (jwtTokenValidator.validateToken(token)) {
                        String username = jwtTokenValidator.extractUsername(token);
                        if (username != null) {
                            String path = request.getRequestURI();
                            String method = request.getMethod();
                            String permissionName = jwtTokenValidator.getPermissionNameForPath(path, method);
                            
                            if (jwtTokenValidator.checkAuthorization(username, permissionName)) {
                                // Cache the token with all permissions and signature for future requests
                                cacheTokenWithAllPermissionsAndSignature(token, username);
                                
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.info("SIGNED CACHE MISS - User authenticated and authorized: {} for path: {} (validated with SSO/ACL)", username, path);
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("Access denied - Insufficient permissions");
                                log.warn("SIGNED CACHE MISS - Authorization failed for user: {} for path: {} with permission: {}", username, path, permissionName);
                                return;
                            }
                        }
                    } else {
                        log.debug("SIGNED CACHE MISS - No valid token found in request");
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
    
    private void cacheTokenWithAllPermissionsAndSignature(String token, String username) {
        try {
            // Get all permissions for the user from ACL service
            // This is a simplified version - in real implementation, you'd call ACL service
            // to get all permissions for the user and cache them with signature
            log.debug("Caching signed token with all permissions for user: {}", username);
            
            // For now, we'll cache with basic permissions - in real implementation,
            // you'd get the full permission list from ACL service
            // The SignedTokenData will automatically generate a cryptographic signature
            tokenCacheService.cacheToken(token, username, new ArrayList<>());
            
            log.info("Signed token cached successfully for user: {} with cryptographic signature", username);
        } catch (Exception e) {
            log.error("Error caching signed token with permissions: {}", e.getMessage());
        }
    }
} 