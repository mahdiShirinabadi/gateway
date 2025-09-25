package com.eureka.service1.aspect;

import com.eureka.service1.service.AclService;
import com.eureka.service1.util.ApiPathExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ACL Aspect for automatic permission checking
 * Uses AOP to check permissions before method execution
 */
@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class AclAspect {

    private final AclService aclService;
    private final ApiPathExtractor apiPathExtractor;

    @Around("@annotation(com.eureka.service1.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Get request headers using RequestContextHolder
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            HttpServletRequest request = attributes.getRequest();
            String authenticatedUser = request.getHeader("X-Authenticated-User");
            
            log.debug("Request headers - X-Authenticated-User: {}", authenticatedUser);
            
            if (authenticatedUser == null || authenticatedUser.isEmpty()) {
                log.warn("No authenticated user found in request headers");
                return createErrorResponse("No authenticated user found", HttpStatus.UNAUTHORIZED);
            }

            // Get permission info from annotation
            com.eureka.service1.annotation.RequirePermission annotation = 
                joinPoint.getSignature().getDeclaringType()
                    .getMethod(joinPoint.getSignature().getName(), 
                              ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes())
                    .getAnnotation(com.eureka.service1.annotation.RequirePermission.class);

            String resource = annotation.resource();
            String action = annotation.action();

            // Extract clean API path and HTTP method from request
            String cleanApiPath = apiPathExtractor.extractCleanApiPath(request);
            String httpMethod = request.getMethod();
            
            // If cleanApiPath is empty or just "/", use the action from annotation
            if (cleanApiPath.isEmpty() || cleanApiPath.equals("/")) {
                cleanApiPath = "/" + action;
            }

            log.info("Checking ACL permission for user: {} resource: {} action: {} apiPath: {} method: {}", 
                    authenticatedUser, resource, action, cleanApiPath, httpMethod);

            // Check permission with actual API path and HTTP method
            boolean hasPermission = aclService.hasPermission(authenticatedUser, resource, action, cleanApiPath, httpMethod);
            if (!hasPermission) {
                log.warn("User {} does not have permission for {}:{}", authenticatedUser, resource, action);
                return createErrorResponse("Access denied", HttpStatus.FORBIDDEN);
            }

            log.info("Permission granted for user {} on {}:{}", authenticatedUser, resource, action);
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("Error in ACL aspect: {}", e.getMessage(), e);
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
