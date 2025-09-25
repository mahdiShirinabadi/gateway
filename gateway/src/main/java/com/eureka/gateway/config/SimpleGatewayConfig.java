package com.eureka.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Simple Gateway Configuration
 * Routes requests to appropriate services
 */
@Configuration
@RequiredArgsConstructor
public class SimpleGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // SSO Routes - public access (no authentication required)
                .route("sso-route", r -> r
                        .path("/sso/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri("http://localhost:8081"))

                // Service1 Routes - with authentication
                .route("service1-route", r -> r
                        .path("/service1/**")
                        .filters(f -> f
//                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri("http://localhost:8082/service1"))

                // ACL Routes - public access (no authentication required)
                .route("acl-route", r -> r
                        .path("/acl/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri("http://localhost:8083"))

                // Gateway Health Route - public access
                .route("gateway-health-route", r -> r
                        .path("/api/gateway/health")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri("http://localhost:8080"))

                .build();
    }
}
