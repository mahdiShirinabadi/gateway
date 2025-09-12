package com.eureka.gateway;

import com.eureka.gateway.model.TokenData;
import com.eureka.gateway.service.TokenCacheService;
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
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final WebClient webClient;
    private final TokenCacheService tokenCacheService;

    public AuthenticationFilter(WebClient webClient, TokenCacheService tokenCacheService) {
        super(Config.class);
        this.webClient = webClient;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("No valid Authorization header found");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            String token = authHeader.substring(7);

            // First, check Redis cache for signed token
            TokenData cachedTokenData = tokenCacheService.getCachedToken(token);
            if (cachedTokenData != null) {
                // Use cached data - no need to call SSO or ACL
                String permissionName = getPermissionNameForPath(request.getPath().value(), request.getMethod().name());
                if (cachedTokenData.hasPermission(permissionName)) {
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-Validated-Token", token)
                            .header("X-Authenticated-User", cachedTokenData.getUsername())
                            .header("X-Cache-Hit", "true")
                            .build();
                    System.out.println("GATEWAY CACHE HIT - Request authorized for user: " + cachedTokenData.getUsername() + " on path: " + request.getPath());
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    System.out.println("GATEWAY CACHE HIT - Authorization failed for user: " + cachedTokenData.getUsername() + " on path: " + request.getPath());
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // Cache miss - validate with SSO and ACL, then cache the result
            return validateTokenWithSSO(token)
                    .flatMap(tokenValidation -> {
                        if (tokenValidation.isValid()) {
                            String permissionName = getPermissionNameForPath(request.getPath().value(), request.getMethod().name());
                            return checkAuthorizationWithACL(tokenValidation.getUsername(), permissionName)
                                    .flatMap(authorized -> {
                                        if (authorized) {
                                            // Get all permissions for the user and cache signed token
                                            return getAllPermissionsAndCacheToken(token, tokenValidation.getUsername())
                                                    .flatMap(permissions -> {
                                                        // Cache the signed token with all permissions
                                                        tokenCacheService.cacheToken(token, tokenValidation.getUsername(), permissions);
                                                        
                                                        ServerHttpRequest modifiedRequest = request.mutate()
                                                                .header("X-Validated-Token", token)
                                                                .header("X-Authenticated-User", tokenValidation.getUsername())
                                                                .header("X-Cache-Hit", "false")
                                                                .build();
                                                        System.out.println("GATEWAY CACHE MISS - Request authorized for user: " + tokenValidation.getUsername() + " on path: " + request.getPath() + " - Token cached with " + permissions.size() + " permissions");
                                                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                                    });
                                        } else {
                                            System.out.println("GATEWAY CACHE MISS - Authorization failed for user: " + tokenValidation.getUsername() + " on path: " + request.getPath());
                                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                            return exchange.getResponse().setComplete();
                                        }
                                    });
                        } else {
                            System.out.println("GATEWAY CACHE MISS - Invalid token for request: " + request.getPath());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(throwable -> {
                        System.out.println("Error during authentication: " + throwable.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private Mono<List<String>> getAllPermissionsAndCacheToken(String token, String username) {
        try {
            // Call ACL service to get all permissions for the user
            String aclPermissionsUrl = "http://localhost:8081/api/acl/user-permissions?username=" + username;
            
            return webClient.get()
                    .uri(aclPermissionsUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        if (response != null && response.containsKey("permissions")) {
                            @SuppressWarnings("unchecked")
                            List<Object> permissionsObj = (List<Object>) response.get("permissions");
                            List<String> permissions = permissionsObj.stream()
                                    .map(obj -> obj.toString())
                                    .toList();
                            System.out.println("Retrieved " + permissions.size() + " permissions for user: " + username);
                            return permissions;
                        } else {
                            System.out.println("No permissions found for user: " + username);
                            return List.<String>of();
                        }
                    })
                    .onErrorReturn(List.<String>of());
                    
        } catch (Exception e) {
            System.out.println("Error getting permissions for user " + username + ": " + e.getMessage());
            return Mono.just(List.<String>of());
        }
    }

    private String getPermissionNameForPath(String path, String method) {
        // Convert path to permission name
        String permission = path
                .replaceAll("/", "_")
                .replaceAll("-", "_")
                .toUpperCase();
        
        if (permission.startsWith("_")) {
            permission = permission.substring(1);
        }
        
        return "SERVICE1_" + method + "_" + permission;
    }

    private Mono<TokenValidationResponse> validateTokenWithSSO(String token) {
        return webClient.post()
                .uri("http://localhost:8084/api/auth/validate")
                .header("Content-Type", "application/json")
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .onErrorReturn(new TokenValidationResponse(false, null));
    }

    private Mono<Boolean> checkAuthorizationWithACL(String username, String permissionName) {
        return webClient.post()
                .uri("http://localhost:8081/api/acl/check")
                .header("Content-Type", "application/json")
                .bodyValue(new AclRequest(username, permissionName))
                .retrieve()
                .bodyToMono(AclResponse.class)
                .map(AclResponse::isAuthorized)
                .onErrorReturn(false);
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