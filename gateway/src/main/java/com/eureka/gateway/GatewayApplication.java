package com.eureka.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	// RouteLocator moved to GatewayRoutesConfig.java for better organization

	@Bean
	public GlobalFilter customGlobalFilter() {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			String path = request.getPath().value();
			String method = request.getMethod().name();
			String clientIp = getClientIp(request);
			String userAgent = request.getHeaders().getFirst("User-Agent");
			String authHeader = request.getHeaders().getFirst("Authorization");
			String requestId = java.util.UUID.randomUUID().toString();
			
			// Log request details
			System.out.println("=== GATEWAY REQUEST [" + requestId + "] ===");
			System.out.println("Method: " + method);
			System.out.println("Path: " + path);
			System.out.println("Client IP: " + clientIp);
			System.out.println("User-Agent: " + userAgent);
			System.out.println("Authorization: " + (authHeader != null ? "Present" : "Not Present"));
			System.out.println("Headers: " + request.getHeaders());
			System.out.println("Query Params: " + request.getQueryParams());
			System.out.println("Timestamp: " + System.currentTimeMillis());
			System.out.println("========================");
			
			// Add request timestamp and ID
			ServerHttpRequest modifiedRequest = request.mutate()
					.header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()))
					.header("X-Gateway-Request-ID", requestId)
					.build();
			
			return chain.filter(exchange.mutate().request(modifiedRequest).build())
					.doOnSuccess(result -> {
						// Log successful response
						System.out.println("=== GATEWAY RESPONSE [" + requestId + "] ===");
						System.out.println("Method: " + method);
						System.out.println("Path: " + path);
						System.out.println("Status: " + exchange.getResponse().getStatusCode());
						System.out.println("Response Headers: " + exchange.getResponse().getHeaders());
						System.out.println("Timestamp: " + System.currentTimeMillis());
						System.out.println("=========================");
					})
					.doOnError(throwable -> {
						// Log error response
						System.out.println("=== GATEWAY ERROR [" + requestId + "] ===");
						System.out.println("Method: " + method);
						System.out.println("Path: " + path);
						System.out.println("Error: " + throwable.getMessage());
						System.out.println("Error Type: " + throwable.getClass().getSimpleName());
						System.out.println("Error Source: GATEWAY");
						System.out.println("Timestamp: " + System.currentTimeMillis());
						System.out.println("=========================");
					})
					.doFinally(signalType -> {
						// Log completion
						System.out.println("=== GATEWAY COMPLETION [" + requestId + "] ===");
						System.out.println("Method: " + method);
						System.out.println("Path: " + path);
						System.out.println("Signal Type: " + signalType);
						System.out.println("Timestamp: " + System.currentTimeMillis());
						System.out.println("=========================");
					});
		};
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

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList("*"));
		corsConfig.setMaxAge(3600L);
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		corsConfig.setAllowedHeaders(Arrays.asList("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsWebFilter(source);
	}


}


