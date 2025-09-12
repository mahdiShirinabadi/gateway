package com.eureka.service1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GatewayHeaderLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Log all headers from Gateway
        logGatewayHeaders(request);
        
        return true;
    }

    private void logGatewayHeaders(HttpServletRequest request) {
        Map<String, String> gatewayHeaders = new HashMap<>();
        Map<String, String> allHeaders = new HashMap<>();
        
        // Get all headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            allHeaders.put(headerName, headerValue);
            
            // Check if it's a Gateway header
            if (isGatewayHeader(headerName)) {
                gatewayHeaders.put(headerName, headerValue);
            }
        }
        
        // Log Gateway specific headers
        if (!gatewayHeaders.isEmpty()) {
            log.info("=== GATEWAY HEADERS RECEIVED ===");
            log.info("Request Path: {}", request.getRequestURI());
            log.info("Request Method: {}", request.getMethod());
            log.info("Gateway Headers Count: {}", gatewayHeaders.size());
            
            gatewayHeaders.forEach((name, value) -> {
                // Mask sensitive token data
                String logValue = maskSensitiveData(name, value);
                log.info("Gateway Header - {}: {}", name, logValue);
            });
            
            log.info("=== END GATEWAY HEADERS ===");
        }
        
        // Log all headers for debugging (optional)
        log.debug("=== ALL REQUEST HEADERS ===");
        allHeaders.forEach((name, value) -> {
            String logValue = maskSensitiveData(name, value);
            log.debug("Header - {}: {}", name, logValue);
        });
        log.debug("=== END ALL HEADERS ===");
    }
    
    private boolean isGatewayHeader(String headerName) {
        return headerName.startsWith("X-") || 
               headerName.equals("Authorization") ||
               headerName.equals("User-Agent") ||
               headerName.equals("Host") ||
               headerName.equals("Content-Type") ||
               headerName.equals("Accept");
    }
    
    private String maskSensitiveData(String headerName, String headerValue) {
        if (headerName.equals("Authorization") || 
            headerName.equals("X-Validated-Token") ||
            headerName.contains("Token")) {
            
            if (headerValue != null && headerValue.length() > 10) {
                return headerValue.substring(0, 10) + "***MASKED***";
            }
        }
        
        return headerValue;
    }
}
