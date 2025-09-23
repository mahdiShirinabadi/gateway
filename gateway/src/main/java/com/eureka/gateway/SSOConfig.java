package com.eureka.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SSOConfig {

    @Value("${sso.service.url:http://localhost:8081/validate-token}")
    private String ssoServiceUrl;

    @Value("${sso.service.timeout:5000}")
    private int ssoServiceTimeout;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(ssoServiceUrl)
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    public String getSsoServiceUrl() {
        return ssoServiceUrl;
    }

    public int getSsoServiceTimeout() {
        return ssoServiceTimeout;
    }
} 