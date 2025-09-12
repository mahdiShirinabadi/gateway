package com.eureka.service1.controller;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/app1")
public class App1Controller {
    
    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('SERVICE1_HELLO_ACCESS')")
    public ResponseEntity<String> hello(@RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
                                      @RequestHeader(value = "X-Validated-Token", required = false) String gatewayToken,
                                      @RequestHeader(value = "X-Gateway-Source", required = false) String gatewaySource,
                                      @RequestHeader(value = "X-Request-Timestamp", required = false) String requestTimestamp,
                                      @RequestHeader(value = "X-Cache-Hit", required = false) String cacheHit,
                                      @RequestHeader(value = "Authorization", required = false) String authorization,
                                      HttpServletRequest request) {
        
        // Get authenticated user from Spring Security context (for direct access)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String directUser = authentication != null ? authentication.getName() : "anonymous";
        
        // Log all Gateway headers for this specific endpoint
        logGatewayHeadersForEndpoint(request, "hello");
        
        log.info("Hello endpoint called - Gateway User: {}, Direct User: {}", gatewayUser, directUser);
        
        StringBuilder response = new StringBuilder();
        response.append("Hello From Service 1!\n");
        response.append("Authentication & Authorization Info:\n");
        response.append("- Gateway User: ").append(gatewayUser != null ? gatewayUser : "None").append("\n");
        response.append("- Direct User: ").append(directUser).append("\n");
        response.append("- Token Present: ").append(gatewayToken != null ? "Yes" : "No").append("\n");
        response.append("- Authentication Method: ").append(gatewayUser != null ? "Gateway" : "Direct").append("\n");
        response.append("- Is Authenticated: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Required Permission: SERVICE1_HELLO_ACCESS\n");
        response.append("- Access Granted: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('SERVICE1_ADMIN_ACCESS')")
    public ResponseEntity<String> admin(@RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String directUser = authentication != null ? authentication.getName() : "anonymous";
        
        log.info("Admin endpoint called - Gateway User: {}, Direct User: {}", gatewayUser, directUser);
        
        StringBuilder response = new StringBuilder();
        response.append("Admin Panel!\n");
        response.append("Authentication & Authorization Info:\n");
        response.append("- Gateway User: ").append(gatewayUser != null ? gatewayUser : "None").append("\n");
        response.append("- Direct User: ").append(directUser).append("\n");
        response.append("- Authentication Method: ").append(gatewayUser != null ? "Gateway" : "Direct").append("\n");
        response.append("- Is Authenticated: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Required Permission: SERVICE1_ADMIN_ACCESS\n");
        response.append("- Access Granted: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Welcome ").append(directUser).append("! You have admin access.");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/user-info")
    @PreAuthorize("hasAuthority('SERVICE1_ALL_ACCESS')")
    public ResponseEntity<String> getUserInfo(@RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
                                           @RequestHeader(value = "X-Validated-Token", required = false) String gatewayToken) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String directUser = authentication != null ? authentication.getName() : "anonymous";
        
        log.info("User info requested - Gateway User: {}, Direct User: {}", gatewayUser, directUser);
        
        StringBuilder response = new StringBuilder();
        response.append("User Information:\n");
        response.append("- Gateway User: ").append(gatewayUser != null ? gatewayUser : "None").append("\n");
        response.append("- Direct User: ").append(directUser).append("\n");
        response.append("- Token Present: ").append(gatewayToken != null ? "Yes" : "No").append("\n");
        response.append("- Service: Service1\n");
        response.append("- Endpoint: /app1/user-info\n");
        response.append("- Authentication Method: ").append(gatewayUser != null ? "Gateway" : "Direct").append("\n");
        response.append("- Is Authenticated: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Required Permission: SERVICE1_ALL_ACCESS\n");
        response.append("- Access Granted: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Authorities: ").append(authentication != null ? authentication.getAuthorities() : "None");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/security-test")
    @PreAuthorize("hasAuthority('SERVICE1_ALL_ACCESS')")
    public ResponseEntity<String> securityTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        StringBuilder response = new StringBuilder();
        response.append("Security Test Results:\n");
        response.append("- Is Authenticated: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Username: ").append(authentication != null ? authentication.getName() : "None").append("\n");
        response.append("- Authorities: ").append(authentication != null ? authentication.getAuthorities() : "None").append("\n");
        response.append("- Principal: ").append(authentication != null ? authentication.getPrincipal() : "None").append("\n");
        response.append("- Authentication Type: ").append(authentication != null ? authentication.getClass().getSimpleName() : "None").append("\n");
        response.append("- Credentials: ").append(authentication != null ? authentication.getCredentials() : "None").append("\n");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/authorization-test")
    @PreAuthorize("hasAuthority('SERVICE1_ALL_ACCESS')")
    public ResponseEntity<String> authorizationTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        StringBuilder response = new StringBuilder();
        response.append("Authorization Test Results:\n");
        response.append("- Current Endpoint: /app1/authorization-test\n");
        response.append("- Required Permission: SERVICE1_ALL_ACCESS\n");
        response.append("- User: ").append(authentication != null ? authentication.getName() : "None").append("\n");
        response.append("- Has Access: ").append(authentication != null && authentication.isAuthenticated()).append("\n");
        response.append("- Authentication Method: Direct (Service1 Security)\n");
        response.append("- Both Auth & Authz Checked: Yes\n");
        response.append("- SSO Validation: Yes\n");
        response.append("- ACL Permission Check: Yes\n");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        StringBuilder response = new StringBuilder();
        response.append("Public Endpoint - No Authentication Required!\n");
        response.append("- This endpoint is accessible without authentication\n");
        response.append("- No @PreAuthorize annotation\n");
        response.append("- No permission check required\n");
        
        return ResponseEntity.ok(response.toString());
    }
    
    @GetMapping("/headers")
    @PreAuthorize("hasAuthority('SERVICE1_ALL_ACCESS')")
    public ResponseEntity<Map<String, Object>> showAllHeaders(HttpServletRequest request) {
        logGatewayHeadersForEndpoint(request, "headers");
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> allHeaders = new HashMap<>();
        Map<String, String> gatewayHeaders = new HashMap<>();
        
        // Get all headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            allHeaders.put(headerName, headerValue);
            
            if (isGatewayHeader(headerName)) {
                gatewayHeaders.put(headerName, maskSensitiveData(headerName, headerValue));
            }
        }
        
        response.put("endpoint", "/app1/headers");
        response.put("requestMethod", request.getMethod());
        response.put("requestURI", request.getRequestURI());
        response.put("remoteAddress", request.getRemoteAddr());
        response.put("totalHeaders", allHeaders.size());
        response.put("gatewayHeaders", gatewayHeaders);
        response.put("allHeaders", allHeaders);
        response.put("message", "تمام header های دریافتی از Gateway");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/super-admin")
    @PreAuthorize("hasAuthority('SERVICE1_SUPER_ADMIN_ACCESS')")
    public ResponseEntity<String> superAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String directUser = authentication != null ? authentication.getName() : "anonymous";
        
        StringBuilder response = new StringBuilder();
        response.append("Super Admin Panel!\n");
        response.append("- User: ").append(directUser).append("\n");
        response.append("- Required Permission: SERVICE1_SUPER_ADMIN_ACCESS\n");
        response.append("- This is the highest level of access\n");
        response.append("- Only super admin users can access this endpoint\n");
        
        return ResponseEntity.ok(response.toString());
    }
    
    // New endpoints to demonstrate automatic discovery
    
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('SERVICE1_POST_APP1_USERS')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        log.info("Creating new user: {}", userData.get("username"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("userId", "user_" + System.currentTimeMillis());
        response.put("username", userData.get("username"));
        response.put("status", "active");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('SERVICE1_PUT_APP1_USERS_USERID')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> userData) {
        log.info("Updating user: {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User updated successfully");
        response.put("userId", userId);
        response.put("updatedFields", userData.keySet());
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('SERVICE1_DELETE_APP1_USERS_USERID')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        log.info("Deleting user: {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("userId", userId);
        response.put("deletedAt", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('SERVICE1_GET_APP1_REPORTS')")
    public ResponseEntity<Map<String, Object>> getReports() {
        log.info("Generating reports");
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", 150);
        response.put("activeUsers", 120);
        response.put("reports", Arrays.asList("user_activity", "system_health", "security_audit"));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAuthority('SERVICE1_POST_APP1_REPORTS_GENERATE')")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody Map<String, Object> reportRequest) {
        log.info("Generating custom report: {}", reportRequest.get("reportType"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("reportId", "report_" + System.currentTimeMillis());
        response.put("reportType", reportRequest.get("reportType"));
        response.put("status", "generating");
        response.put("estimatedCompletion", System.currentTimeMillis() + 30000);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/log-test")
    @PreAuthorize("hasAuthority('SERVICE1_ALL_ACCESS')")
    public ResponseEntity<Map<String, Object>> testLogging(HttpServletRequest request) {
        logGatewayHeadersForEndpoint(request, "log-test");
        
        // Test different log levels
        log.trace("This is a TRACE level message - should not appear in console");
        log.debug("This is a DEBUG level message - should not appear in console");
        log.info("This is an INFO level message - should appear in console and files");
        log.warn("This is a WARN level message - should appear in console and files");
        log.error("This is an ERROR level message - should appear in console, files, and error file");
        
        // Test structured logging
        log.info("Structured logging test - User: {}, Action: {}, Timestamp: {}", 
                "test_user", "log_test", System.currentTimeMillis());
        
        // Test exception logging
        try {
            throw new RuntimeException("Test exception for logging");
        } catch (Exception e) {
            log.error("Test exception caught and logged", e);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Log4j2 logging test completed");
        response.put("timestamp", System.currentTimeMillis());
        response.put("logLevels", Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR"));
        response.put("logFiles", Arrays.asList(
            "logs/service1/service1.log",
            "logs/service1/gateway-headers.log", 
            "logs/service1/errors.log"
        ));
        response.put("note", "Check console output and log files for test results");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to log all Gateway headers for a specific endpoint
     */
    private void logGatewayHeadersForEndpoint(HttpServletRequest request, String endpointName) {
        log.info("=== GATEWAY HEADERS FOR ENDPOINT: {} ===", endpointName);
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Request Method: {}", request.getMethod());
        log.info("Remote Address: {}", request.getRemoteAddr());
        
        // Log all headers
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> gatewayHeaders = new HashMap<>();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Check if it's a Gateway-related header
            if (isGatewayHeader(headerName)) {
                gatewayHeaders.put(headerName, headerValue);
            }
        }
        
        // Log Gateway headers
        if (!gatewayHeaders.isEmpty()) {
            log.info("Gateway Headers Count: {}", gatewayHeaders.size());
            gatewayHeaders.forEach((name, value) -> {
                String logValue = maskSensitiveData(name, value);
                log.info("Gateway Header - {}: {}", name, logValue);
            });
        } else {
            log.info("No Gateway headers found");
        }
        
        log.info("=== END GATEWAY HEADERS FOR ENDPOINT: {} ===", endpointName);
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