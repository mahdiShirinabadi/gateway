# API Gateway

A Spring Cloud Gateway-based API gateway with advanced features for microservices architecture.

## Features

### ✅ Core Features
- **Service Discovery**: Integrates with Netflix Eureka for dynamic service discovery
- **Load Balancing**: Automatic load balancing for service instances
- **Circuit Breaker**: Resilience4j circuit breaker for fault tolerance
- **CORS Support**: Cross-Origin Resource Sharing configuration
- **Request Logging**: Global filter for request/response logging
- **Health Checks**: Built-in health check endpoints
- **Fallback Handling**: Graceful degradation when services are unavailable

### 🔧 Advanced Features
- **Custom Route Configuration**: Java-based route definitions with filters
- **Path Rewriting**: URL path transformation for backend services
- **Request Headers**: Automatic addition of gateway identification headers
- **SSO Authentication**: Token validation with external SSO service
- **Management Endpoints**: Exposed actuator endpoints for monitoring

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │───▶│   API Gateway   │───▶│   Microservice  │
│                 │    │   (Port 8080)   │    │   (SERVICE1)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Eureka Server │
                       │   (Port 8761)   │
                       └─────────────────┘
```

## Configuration

### Routes
- **Service1**: `/app1/**` → `lb://SERVICE1` (Load balanced)
- **Service2**: `/app2/**` → `http://SERVICE2` (Direct)
- **Health**: `/health/**` → Local health endpoint

### Circuit Breaker Settings
- Sliding window size: 10 requests
- Minimum calls: 5
- Failure rate threshold: 50%
- Wait duration in open state: 5 seconds

## Getting Started

### Prerequisites
- Java 21
- Maven 3.6+
- Eureka Server running on port 8761

### Running the Gateway

1. **Start Eureka Server** (if not already running):
   ```bash
   # Start your Eureka server on port 8761
   ```

2. **Build and Run the Gateway**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Verify the Gateway**:
   ```bash
   curl http://localhost:8080/health
   ```

### Testing the Routes

1. **Test Service1 Route** (requires authentication):
   ```bash
   curl -H "Authorization: Bearer your-jwt-token" http://localhost:8080/app1/api/test
   ```

2. **Test Service2 Route** (requires authentication):
   ```bash
   curl -H "Authorization: Bearer your-jwt-token" http://localhost:8080/app2/api/test
   ```

3. **Test without Authentication** (will return 401):
   ```bash
   curl http://localhost:8080/app1/api/test
   ```

4. **Test Authentication Flow**:
   ```bash
   curl -H "Authorization: Bearer your-jwt-token" http://localhost:8080/test-auth
   ```

## Management Endpoints

- **Health Check**: `GET /health`
- **Gateway Info**: `GET /actuator/gateway`
- **Health Details**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`

## Monitoring and Logging

### Logs
The gateway logs all requests and responses with timestamps:
```
Gateway Request: GET /app1/api/test
Gateway Response: GET /app1/api/test - onComplete
```

### Circuit Breaker Metrics
Monitor circuit breaker states through actuator endpoints:
```bash
curl http://localhost:8080/actuator/health
```

## Customization

### Adding New Routes
Edit `GatewayApplication.java` and add new routes to the `customRouteLocator` method:

```java
.route("new-service", r -> r
    .path("/new/**")
    .filters(f -> f
        .rewritePath("/new/(?<segment>.*)", "/${segment}")
        .addRequestHeader("X-Gateway-Source", "api-gateway"))
    .uri("lb://NEW-SERVICE"))
```

### SSO Authentication Configuration
The gateway validates tokens with an external SSO service:

1. **Configure SSO Service URL** in `application.properties`:
   ```properties
   sso.service.url=http://your-sso-service:8081/validate-token
   ```

2. **SSO Service Expected Response**:
   ```json
   {
     "valid": true,
     "message": "Token is valid"
   }
   ```

3. **Token Flow**:
   - Client sends request with `Authorization: Bearer <token>`
   - Gateway validates token with SSO service
   - If valid: adds `X-Validated-Token` and `X-Authenticated-User` headers
   - If invalid: returns 401 Unauthorized

### Adding Rate Limiting
To add rate limiting, you'll need to:
1. Add Redis dependency to `pom.xml`
2. Configure Redis connection in `application.properties`
3. Implement proper rate limiter beans

## Troubleshooting

### Common Issues

1. **Service Not Found**: Ensure Eureka server is running and services are registered
2. **Circuit Breaker Open**: Check if backend services are healthy
3. **CORS Issues**: Verify CORS configuration in `corsWebFilter` bean

### Debug Mode
Enable debug logging by setting in `application.properties`:
```properties
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.reactor.netty=DEBUG
```

## Dependencies

- Spring Boot 3.5.3
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Resilience4j Circuit Breaker
- Spring Boot Actuator

## License

This project is licensed under the MIT License. 