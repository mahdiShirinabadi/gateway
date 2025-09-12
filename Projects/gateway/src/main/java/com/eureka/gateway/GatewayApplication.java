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

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationFilter authenticationFilter) {
		return builder.routes()
				.route("service1", r -> r
						.path("/app1/**")
						.filters(f -> f
								.rewritePath("/app1/(?<segment>.*)", "/${segment}")
								.addRequestHeader("X-Gateway-Source", "api-gateway")
								.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
								.circuitBreaker(config -> config
										.setName("service1-circuit-breaker")
										.setFallbackUri("forward:/fallback")))
						.uri("lb://SERVICE1"))
				.route("service2", r -> r
						.path("/app2/**")
						.filters(f -> f
								.rewritePath("/app2/(?<segment>.*)", "/${segment}")
								.addRequestHeader("X-Gateway-Source", "api-gateway")
								.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
								.circuitBreaker(config -> config
										.setName("service2-circuit-breaker")
										.setFallbackUri("forward:/fallback")))
						.uri("http://SERVICE2"))
				.route("health", r -> r
						.path("/health/**")
						.uri("http://localhost:8080"))
				.build();
	}

	@Bean
	public GlobalFilter customGlobalFilter() {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			String path = request.getPath().value();
			String method = request.getMethod().name();
			
			System.out.println("Gateway Request: " + method + " " + path);
			
			// Add request timestamp
			ServerHttpRequest modifiedRequest = request.mutate()
					.header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()))
					.build();
			
			return chain.filter(exchange.mutate().request(modifiedRequest).build())
					.doFinally(signalType -> {
						System.out.println("Gateway Response: " + method + " " + path + " - " + signalType);
					});
		};
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


