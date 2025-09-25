package com.eureka.gateway.config;

import com.eureka.gateway.model.TokenValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * Simple Authentication Filter for Gateway
 * Validates tokens with SSO service without Redis
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class SimpleAuthenticationFilter implements WebFilter {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String token = extractTokenFromRequest(exchange);
        if (token == null) {
            log.warn("No token found in request for path: {}", path);
            return unauthorizedResponse(exchange);
        }

        // Validate token with SSO service
        return validateTokenWithSSO(token)
                .flatMap(validationResponse -> {
                    if (validationResponse.isValid()) {
                        log.info("Token validated successfully for path: {}", path);
                        
                        // Extract username from token and add to headers
                        String username = extractUsernameFromToken(token);
                        log.info("Extracted username: {} for path: {}", username, path);

                        // Add username and token to request headers for downstream services
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(exchange.getRequest().mutate()
                                        .header("X-Authenticated-User", username)
                                        .header("X-Auth-Token", token)
                                        .header("X-User-Info", username)
                                        .build())
                                .build();

                        return chain.filter(modifiedExchange);
                    } else {
                        log.warn("Token validation failed for path: {}", path);
                        return unauthorizedResponse(exchange);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error validating token: {}", e.getMessage());
                    return unauthorizedResponse(exchange);
                });
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/gateway/health") ||
               path.startsWith("/api/gateway/public") ||
               path.startsWith("/actuator") ||
               path.startsWith("/sso/") ||
               path.startsWith("/acl/");
    }

    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractUsernameFromToken(String token) {
        try {
            // Simple JWT token parsing to extract username
            // Split token into parts and decode payload
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length == 3) {
                // Decode the payload (second part)
                String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
                
                // Parse JSON to extract username
                // Simple JSON parsing - in production use a proper JSON library
                if (payload.contains("\"sub\"")) {
                    // Extract username from "sub" field
                    int startIndex = payload.indexOf("\"sub\":\"") + 7;
                    int endIndex = payload.indexOf("\"", startIndex);
                    if (startIndex > 6 && endIndex > startIndex) {
                        String username = payload.substring(startIndex, endIndex);
                        log.debug("Extracted username from token: {}", username);
                        return username;
                    }
                }
            }
            
            log.warn("Could not extract username from token");
            return "unknown";
            
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return "unknown";
        }
    }

    private Mono<TokenValidationResponse> validateTokenWithSSO(String token) {
      /*  return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/api/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(result -> log.debug("SSO validation result: {}", result))
                .doOnError(error -> log.error("SSO validation error: {}", error.getMessage()));*/

        return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonMap("token", token))  // send token in body
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .doOnSuccess(result -> log.info("SSO validation result: {}", result))
                .doOnError(error -> log.error("SSO validation error: {}", error.getMessage()));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String responseBody = "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing token\"}";
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))
        );
    }
}
