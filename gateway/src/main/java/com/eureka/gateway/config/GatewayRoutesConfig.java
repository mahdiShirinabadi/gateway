package com.eureka.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Routes Configuration
 * Modern approach for Spring Cloud Gateway 2025.0.0
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Service1 Route
                .route("service1-route", r -> r
                        .path("/service1/**")
//                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082/service1"))
                
                // Service2 Route
                .route("service2-route", r -> r
                        .path("/service2/**")
//                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8083"))
                
                // ACL Route
                .route("acl-route", r -> r
                        .path("/acl/**")
//                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081/acl"))
                
                // SSO Route
                .route("sso-route", r -> r
                        .path("/sso/**")
//                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8084/sso"))
                
                .build();
    }
}
