package com.eureka.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient webClient;

    public AuthenticationFilter(WebClient webClient) {
        super(Config.class);
        this.webClient = webClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check if the request has an Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Request without Authorization header: " + request.getPath());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Extract token from Authorization header
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate token with SSO service
            return validateTokenWithSSO(token)
                    .flatMap(tokenValidation -> {
                        if (tokenValidation.isValid()) {
                            // Token is valid, now check authorization with ACL
                            String permissionName = getPermissionNameForPath(request.getPath().value(), request.getMethod().name());
                            return checkAuthorizationWithACL(tokenValidation.getUsername(), permissionName)
                                    .flatMap(authorized -> {
                                        if (authorized) {
                                            // Both authentication and authorization successful
                                            ServerHttpRequest modifiedRequest = request.mutate()
                                                    .header("X-Validated-Token", token)
                                                    .header("X-Authenticated-User", tokenValidation.getUsername())
                                                    .build();
                                            
                                            System.out.println("Request authorized for user: " + tokenValidation.getUsername() + " on path: " + request.getPath());
                                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                        } else {
                                            // Authorization failed
                                            System.out.println("Authorization failed for user: " + tokenValidation.getUsername() + " on path: " + request.getPath());
                                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                            return exchange.getResponse().setComplete();
                                        }
                                    });
                        } else {
                            // Token is invalid, return 401
                            System.out.println("Invalid token for request: " + request.getPath());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(throwable -> {
                        System.out.println("Error during authentication/authorization: " + throwable.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private String getPermissionNameForPath(String path, String method) {
        // Map paths to permission names
        if (path.startsWith("/service1/app1/hello")) {
            return "SERVICE1_HELLO_ACCESS";
        } else if (path.startsWith("/service1/app1/admin")) {
            return "SERVICE1_ADMIN_ACCESS";
        } else if (path.startsWith("/service1/")) {
            return "SERVICE1_ALL_ACCESS";
        }
        return "DEFAULT_PERMISSION";
    }

    private Mono<TokenValidationResponse> validateTokenWithSSO(String token) {
        return webClient.post()
                .uri("http://localhost:8081/api/auth/validate")
                .header("Content-Type", "application/json")
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .onErrorReturn(new TokenValidationResponse(false, "Token validation failed", null));
    }

    private Mono<Boolean> checkAuthorizationWithACL(String username, String permissionName) {
        return webClient.post()
                .uri("http://localhost:8083/api/acl/check")
                .header("Content-Type", "application/json")
                .bodyValue(new AclRequest(username, permissionName))
                .retrieve()
                .bodyToMono(AclResponse.class)
                .map(AclResponse::isAllowed)
                .onErrorReturn(false);
    }

    public static class Config {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Request/Response classes for SSO validation
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
        private String message;
        private String username;

        public TokenValidationResponse() {}

        public TokenValidationResponse(boolean valid, String message, String username) {
            this.valid = valid;
            this.message = message;
            this.username = username;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    // Request/Response classes for ACL authorization
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
        private String username;
        private String permission;
        private boolean allowed;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }
    }
} 