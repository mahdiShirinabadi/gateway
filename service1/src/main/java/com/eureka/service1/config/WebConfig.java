package com.eureka.service1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GatewayHeaderLoggingInterceptor gatewayHeaderLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayHeaderLoggingInterceptor)
                .addPathPatterns("/**")  // Apply to all paths
                .excludePathPatterns(
                        "/actuator/**",  // Exclude actuator endpoints
                        "/error",        // Exclude error pages
                        "/favicon.ico"   // Exclude favicon
                );
    }
}

