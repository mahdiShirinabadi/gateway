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
                    // Cache miss - token not found in Redis
                    // Service1 does not store tokens - Gateway should have stored it
                    log.warn("CACHE MISS - Token not found in Redis. Gateway should have stored token info for: {}", token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token not found - please re-authenticate");
                    return;
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
    
    // Note: Service1 does not store tokens in Redis - Gateway handles token storage
} 