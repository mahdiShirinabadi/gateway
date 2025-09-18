package com.eureka.gateway.config;

import com.eureka.gateway.AuthenticationFilter;
import com.eureka.gateway.filter.RequestLoggingFilter;
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
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationFilter authenticationFilter, RequestLoggingFilter requestLoggingFilter) {
        return builder.routes()
                // SSO Routes - mixed public and protected access
                .route("sso-route", r -> r
                        .path("/sso/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .filter((exchange, chain) -> {
                                    String path = exchange.getRequest().getPath().value();
                                    // Public paths (no authentication required)
                                    if (path.matches("/api/auth/(login|public-key|health)$")) {
                                        return chain.filter(exchange);
                                    }
                                    // Protected paths (authentication required)
                                    else if (path.startsWith("/api/users/") || path.equals("/api/auth/validate")) {
                                        return authenticationFilter.apply(new AuthenticationFilter.Config())
                                                .filter(exchange, chain);
                                    }
                                    // Default to no authentication for other SSO paths
                                    return chain.filter(exchange);
                                }))
                        .uri("http://localhost:8084"))

                // Service1 Route - with authentication
                .route("service1-route", r -> r
                        .path("/service1/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))

                // Service2 Route - with authentication
                .route("service2-route", r -> r
                        .path("/service2/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))

                // ACL Project Registration Routes - public access (no authentication)
                .route("acl-project-route", r -> r
                        .path("/acl/api/project-registration/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri("http://localhost:8083/acl"))
                
                // ACL Other Routes - with authentication
                .route("acl-route", r -> r
                        .path("/acl/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://localhost:8083/acl"))

                .build();
    }
}
