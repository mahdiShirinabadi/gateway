package com.eureka.gateway;

import com.eureka.gateway.model.TokenData;
import com.eureka.gateway.service.TokenCacheService;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final WebClient webClient;
    private final TokenCacheService tokenCacheService;

    /**
     * Constructor for AuthenticationFilter
     * Initializes the filter with WebClient for external service calls and TokenCacheService for Redis operations
     * 
     * @param webClient WebClient instance for making HTTP calls to SSO and ACL services
     * @param tokenCacheService Service for managing token caching in Redis
     */
    public AuthenticationFilter(WebClient webClient, TokenCacheService tokenCacheService) {
        super(Config.class);
        this.webClient = webClient;
        this.tokenCacheService = tokenCacheService;
        log.info("AuthenticationFilter initialized with WebClient and TokenCacheService");
    }

    /**
     * Main authentication filter method
     * Validates JWT tokens and checks user permissions for incoming requests
     * 
     * Flow:
     * 1. Extract Authorization header
     * 2. Check Redis cache for token validation
     * 3. If not cached, validate with SSO service
     * 4. Check permissions with ACL service
     * 5. Cache validation result for future requests
     * 
     * @param config Filter configuration
     * @return GatewayFilter instance
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("=== GATEWAY AUTHENTICATION FILTER START ===");
            
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().name();
            String clientIp = getClientIp(request);
            
            log.info("Processing authentication for request: {} {} from IP: {}", method, path, clientIp);
            
            // Extract Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            log.debug("Authorization header present: {}", authHeader != null);

            // Check if Authorization header exists and has Bearer format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("=== AUTHENTICATION ERROR ===");
                log.warn("Error: No valid Authorization header found");
                log.warn("Error Source: GATEWAY AUTHENTICATION FILTER");
                log.warn("Status: 401 UNAUTHORIZED");
                log.warn("Path: {}", path);
                log.warn("Method: {}", method);
                log.warn("Client IP: {}", clientIp);
                log.warn("=============================");
                
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Extract token from Authorization header
            String token = authHeader.substring(7);
            log.debug("Token extracted from Authorization header, length: {}", token.length());

            // Check Redis cache for token validation first (performance optimization)
            log.debug("Checking Redis cache for token validation");
            TokenData cachedTokenData = tokenCacheService.getCachedToken(token);
            
            if (cachedTokenData != null && !cachedTokenData.isExpired()) {
                log.info("Token found in Redis cache and not expired");
                log.debug("Cached token data: user={}, expires={}", cachedTokenData.getUsername(), cachedTokenData.getExpiresAt());
                
                // Use cached data - no need to call SSO or ACL (performance optimization)
                String permissionName = getPermissionNameForPath(path, method);
                log.debug("Required permission for path {}: {}", path, permissionName);
                
                if (cachedTokenData.hasPermission(permissionName)) {
                    log.info("User {} has required permission {} - request authorized", cachedTokenData.getUsername(), permissionName);
                    
                    // Add security headers for downstream services
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-Validated-Token", token)
                            .header("X-Authenticated-User", cachedTokenData.getUsername())
                            .header("X-Cache-Hit", "true")
                            .header("X-Token-Expires", String.valueOf(cachedTokenData.getExpiresAt()))
                            .build();
                    
                    log.info("GATEWAY CACHE HIT - Request authorized for user: {} on path: {}", cachedTokenData.getUsername(), path);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    log.warn("=== AUTHORIZATION ERROR ===");
                    log.warn("Error: User does not have required permission");
                    log.warn("Error Source: GATEWAY AUTHENTICATION FILTER (CACHE)");
                    log.warn("Status: 403 FORBIDDEN");
                    log.warn("User: {}", cachedTokenData.getUsername());
                    log.warn("Path: {}", path);
                    log.warn("Method: {}", method);
                    log.warn("Required Permission: {}", permissionName);
                    log.warn("User Permissions: {}", cachedTokenData.getPermissions());
                    log.warn("=============================");
                    
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            } else if (cachedTokenData != null && cachedTokenData.isExpired()) {
                // Token expired - remove from cache and return 401 (no SSO call needed)
                log.warn("Token found in cache but expired - removing from cache");
                tokenCacheService.removeToken(token);
                
                log.warn("=== TOKEN EXPIRED ===");
                log.warn("Error: Token has expired");
                log.warn("Error Source: GATEWAY AUTHENTICATION FILTER");
                log.warn("Status: 401 UNAUTHORIZED");
                log.warn("Path: {}", path);
                log.warn("Method: {}", method);
                log.warn("Token expired at: {}", cachedTokenData.getExpiresAt());
                log.warn("=============================");
                
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Cache miss - validate with SSO and ACL, then cache the result
            log.info("Token not found in cache - validating with SSO service");
            return validateTokenWithSSO(token)
                        .flatMap(tokenValidation -> {
                            log.debug("SSO validation result: valid={}, username={}", tokenValidation.isValid(), tokenValidation.getUsername());
                            
                            if (tokenValidation.isValid()) {
                                String permissionName = getPermissionNameForPath(path, method);
                                log.debug("Checking authorization with ACL for permission: {}", permissionName);
                                
                                return checkAuthorizationWithACL(tokenValidation.getUsername(), permissionName)
                                        .flatMap(authorized -> {
                                            log.debug("ACL authorization result: {}", authorized);
                                            
                                            if (authorized) {
                                                log.info("User {} authorized for permission {} - getting all permissions", tokenValidation.getUsername(), permissionName);
                                                
                                                // Get all permissions for the user and cache signed token
                                                return getAllPermissionsAndCacheToken(token, tokenValidation.getUsername())
                                                        .flatMap(permissions -> {
                                                            log.info("Retrieved {} permissions for user {}", permissions.size(), tokenValidation.getUsername());
                                                            
                                                            // Cache the signed token with all permissions
                                                            tokenCacheService.cacheToken(token, tokenValidation.getUsername(), permissions);
                                                            log.debug("Token cached in Redis for user: {}", tokenValidation.getUsername());

                                                            // Add security headers for downstream services
                                                            ServerHttpRequest modifiedRequest = request.mutate()
                                                                    .header("X-Validated-Token", token)
                                                                    .header("X-Authenticated-User", tokenValidation.getUsername())
                                                                    .header("X-Cache-Hit", "false")
                                                                    .build();
                                                            
                                                            log.info("GATEWAY CACHE MISS - Request authorized for user: {} on path: {} - Token cached with {} permissions", 
                                                                    tokenValidation.getUsername(), path, permissions.size());
                                                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                                        });
                                            } else {
                                                log.warn("=== AUTHORIZATION ERROR ===");
                                                log.warn("Error: User does not have required permission");
                                                log.warn("Error Source: GATEWAY AUTHENTICATION FILTER (ACL)");
                                                log.warn("Status: 403 FORBIDDEN");
                                                log.warn("User: {}", tokenValidation.getUsername());
                                                log.warn("Path: {}", path);
                                                log.warn("Method: {}", method);
                                                log.warn("Required Permission: {}", permissionName);
                                                log.warn("=============================");
                                                
                                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                                return exchange.getResponse().setComplete();
                                            }
                                        });
                            } else {
                                log.warn("SSO validation failed for token");
                                log.warn("GATEWAY CACHE MISS - Invalid token for request: {}", path);
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }
                        })
                        .onErrorResume(throwable -> {
                            log.error("Error during authentication process", throwable);
                            log.error("Error details: {}", throwable.getMessage());
                            log.error("Error type: {}", throwable.getClass().getSimpleName());
                            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            return exchange.getResponse().setComplete();
                        });
        };
    }

    /**
     * Retrieves all permissions for a user from ACL service
     * This method is called when a token is not in cache and needs to be validated
     * 
     * @param token JWT token for the user
     * @param username Username to get permissions for
     * @return Mono containing list of user permissions
     */
    private Mono<List<String>> getAllPermissionsAndCacheToken(String token, String username) {
        log.debug("Getting all permissions for user: {}", username);
        
        try {
            // Call ACL service to get all permissions for the user
            String aclPermissionsUrl = "http://localhost:8081/api/acl/user-permissions?username=" + username;
            log.debug("Calling ACL service at URL: {}", aclPermissionsUrl);
            
            return webClient.get()
                    .uri(aclPermissionsUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        log.debug("ACL service response received for user: {}", username);
                        
                        if (response != null && response.containsKey("permissions")) {
                            @SuppressWarnings("unchecked")
                            List<Object> permissionsObj = (List<Object>) response.get("permissions");
                            List<String> permissions = permissionsObj.stream()
                                    .map(obj -> obj.toString())
                                    .toList();
                            log.info("Retrieved {} permissions for user: {}", permissions.size(), username);
                            log.debug("User permissions: {}", permissions);
                            return permissions;
                        } else {
                            log.warn("No permissions found for user: {}", username);
                            return List.<String>of();
                        }
                    })
                    .onErrorReturn(List.<String>of());
                    
        } catch (Exception e) {
            log.error("Error getting permissions for user {}: {}", username, e.getMessage());
            log.error("Exception details: ", e);
            return Mono.just(List.<String>of());
        }
    }

    /**
     * Converts a request path and HTTP method to a permission name
     * Used for checking if user has required permission for the requested resource
     * 
     * @param path Request path (e.g., "/service1/test/config/status")
     * @param method HTTP method (e.g., "GET", "POST")
     * @return Permission name (e.g., "SERVICE1_GET_SERVICE1_TEST_CONFIG_STATUS")
     */
    private String getPermissionNameForPath(String path, String method) {
        log.debug("Converting path to permission: {} {}", method, path);
        
        // Convert path to permission name
        String permission = path
                .replaceAll("/", "_")
                .replaceAll("-", "_")
                .toUpperCase();
        
        if (permission.startsWith("_")) {
            permission = permission.substring(1);
        }
        
        String fullPermission = "SERVICE1_" + method + "_" + permission;
        log.debug("Generated permission name: {}", fullPermission);
        
        return fullPermission;
    }

    /**
     * Validates a JWT token with the SSO service
     * This method is called when token is not found in Redis cache
     * 
     * @param token JWT token to validate
     * @return Mono containing validation result with username if valid
     */
    private Mono<TokenValidationResponse> validateTokenWithSSO(String token) {
        log.debug("Validating token with SSO service");
        log.debug("Token length: {}", token.length());
        
        return webClient.post()
                .uri("http://localhost:8084/api/auth/validate")
                .header("Content-Type", "application/json")
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .doOnSuccess(response -> {
                    log.debug("SSO validation successful: valid={}, username={}", response.isValid(), response.getUsername());
                })
                .doOnError(error -> {
                    log.error("SSO validation failed: {}", error.getMessage());
                    log.error("SSO validation error details: ", error);
                })
                .onErrorReturn(new TokenValidationResponse(false, null));
    }

    /**
     * Checks if a user has a specific permission by calling the ACL service
     * This method is called after SSO validation to check authorization
     * 
     * @param username Username to check permission for
     * @param permissionName Permission name to check
     * @return Mono containing boolean result (true if authorized)
     */
    private Mono<Boolean> checkAuthorizationWithACL(String username, String permissionName) {
        log.debug("Checking authorization with ACL for user: {} permission: {}", username, permissionName);
        
        return webClient.post()
                .uri("http://localhost:8081/api/acl/check")
                .header("Content-Type", "application/json")
                .bodyValue(new AclRequest(username, permissionName))
                .retrieve()
                .bodyToMono(AclResponse.class)
                .map(AclResponse::isAuthorized)
                .doOnSuccess(authorized -> {
                    log.debug("ACL authorization result: {} for user: {} permission: {}", authorized, username, permissionName);
                })
                .doOnError(error -> {
                    log.error("ACL authorization check failed for user: {} permission: {}", username, permissionName);
                    log.error("ACL error details: ", error);
                })
                .onErrorReturn(false);
    }

    /**
     * Extracts the client IP address from the request
     * Checks X-Forwarded-For, X-Real-IP headers, and falls back to remote address
     * 
     * @param request ServerHttpRequest object
     * @return Client IP address as string
     */
    private String getClientIp(ServerHttpRequest request) {
        log.debug("Extracting client IP from request");
        
        // Check X-Forwarded-For header (for load balancers/proxies)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String clientIp = xForwardedFor.split(",")[0].trim();
            log.debug("Client IP from X-Forwarded-For: {}", clientIp);
            return clientIp;
        }

        // Check X-Real-IP header (for nginx proxies)
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            log.debug("Client IP from X-Real-IP: {}", xRealIp);
            return xRealIp;
        }

        // Fall back to remote address
        String remoteAddress = request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        log.debug("Client IP from remote address: {}", remoteAddress);
        
        return remoteAddress;
    }

    public static class Config {
        // Configuration properties if needed
    }

    public static class TokenValidationRequest {
        private String token;

        public TokenValidationRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String username;

        public TokenValidationResponse(boolean valid, String username) {
            this.valid = valid;
            this.username = username;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class AclRequest {
        private String username;
        private String permissionName;

        public AclRequest(String username, String permissionName) {
            this.username = username;
            this.permissionName = permissionName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPermissionName() {
            return permissionName;
        }

        public void setPermissionName(String permissionName) {
            this.permissionName = permissionName;
        }
    }

    public static class AclResponse {
        private boolean authorized;

        public AclResponse(boolean authorized) {
            this.authorized = authorized;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public void setAuthorized(boolean authorized) {
            this.authorized = authorized;
        }
    }
} 