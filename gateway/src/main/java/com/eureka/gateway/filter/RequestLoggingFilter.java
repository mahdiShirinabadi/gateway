package com.eureka.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request and Response Logging Filter for Gateway
 */
@Component
public class RequestLoggingFilter extends AbstractGatewayFilterFactory<RequestLoggingFilter.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger("com.eureka.gateway.request");
    
    public RequestLoggingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestId = UUID.randomUUID().toString();
            
            // Add request ID to headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Request-ID", requestId)
                    .header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();
            
            // Log request
            logRequest(requestId, request);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doOnSuccess(result -> {
                        // Log successful response
                        logResponse(requestId, exchange.getResponse(), "SUCCESS");
                    })
                    .doOnError(throwable -> {
                        // Log error response
                        logError(requestId, throwable);
                    })
                    .doFinally(signalType -> {
                        // Log completion
                        logCompletion(requestId, signalType.toString());
                    });
        };
    }
    
    private void logRequest(String requestId, ServerHttpRequest request) {
        logger.info("=== REQUEST [{}] ===", requestId);
        logger.info("Method: {}", request.getMethod());
        logger.info("Path: {}", request.getPath());
        logger.info("Client IP: {}", getClientIp(request));
        logger.info("User-Agent: {}", request.getHeaders().getFirst("User-Agent"));
        logger.info("Authorization: {}", request.getHeaders().getFirst("Authorization") != null ? "Present" : "Not Present");
        logger.info("Headers: {}", request.getHeaders());
        logger.info("Query Params: {}", request.getQueryParams());
        logger.info("Timestamp: {}", System.currentTimeMillis());
        logger.info("========================");
    }
    
    private void logResponse(String requestId, ServerHttpResponse response, String status) {
        logger.info("=== RESPONSE [{}] ===", requestId);
        logger.info("Status: {} ({})", response.getStatusCode(), status);
        logger.info("Response Headers: {}", response.getHeaders());
        logger.info("Timestamp: {}", System.currentTimeMillis());
        logger.info("========================");
    }
    
    private void logError(String requestId, Throwable throwable) {
        logger.error("=== ERROR [{}] ===", requestId);
        logger.error("Error: {}", throwable.getMessage());
        logger.error("Exception: ", throwable);
        logger.error("Timestamp: {}", System.currentTimeMillis());
        logger.error("========================");
    }
    
    private void logCompletion(String requestId, String signalType) {
        logger.info("=== COMPLETION [{}] ===", requestId);
        logger.info("Signal Type: {}", signalType);
        logger.info("Timestamp: {}", System.currentTimeMillis());
        logger.info("========================");
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}


