# Spring Boot 2.4+ Configuration Import

## تغییر از bootstrap.properties به spring.config.import

### مشکل قبلی (❌ Deprecated)
از Spring Boot 2.4+ به بعد، `bootstrap.yml` و `bootstrap.properties` deprecated شده‌اند.

### راه‌حل جدید (✅ Recommended)
استفاده از `spring.config.import` در `application.properties`

## تغییرات اعمال شده

### 1. حذف bootstrap.properties
**قبل**:
```properties
# bootstrap.properties
spring.application.name=service1
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.enabled=true
spring.cloud.config.fail-fast=true
```

**بعد**:
```properties
# application.properties
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=service1
```

### 2. انتقال تنظیمات Eureka
**قبل** (در bootstrap.properties):
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

**بعد** (در application.properties):
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

### 3. حذف Dependency غیرضروری
**قبل**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

**بعد**:
```xml
<!-- حذف شده - دیگر نیازی نیست -->
```

## مزایای spring.config.import

### ✅ سادگی
- **یک فایل**: همه تنظیمات در `application.properties`
- **بدون bootstrap**: نیازی به فایل جداگانه نیست
- **ساده‌تر**: کمتر پیچیده

### ✅ انعطاف‌پذیری
- **Optional**: اگر config-server در دسترس نباشد، سرویس کار می‌کند
- **Multiple Sources**: می‌توان چندین منبع config اضافه کرد
- **Conditional**: می‌توان بر اساس profile تنظیم کرد

### ✅ سازگاری
- **Spring Boot 2.4+**: روش مدرن و توصیه شده
- **Future Proof**: آینده‌نگر و به‌روز
- **Best Practice**: روش استاندارد

## نحوه کارکرد

### 1. شروع سرویس
```
1. Service1 شروع می‌شود
2. application.properties خوانده می‌شود
3. spring.config.import اجرا می‌شود
4. به config-server متصل می‌شود
5. shared.properties و service1.properties لود می‌شوند
6. سرویس با تنظیمات کامل شروع می‌شود
```

### 2. Configuration Loading Order
```
1. application.properties (محلی)
2. config-server (remote)
   - shared.properties
   - service1.properties
3. Environment variables
4. Command line arguments
```

## مثال کامل

### application.properties
```properties
# Server Configuration
server.port=8082
server.servlet.context-path=/service1

# Spring Boot 2.4+ Configuration Import
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=service1

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/service1_db
spring.datasource.username=postgres
spring.datasource.password=password

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### shared.properties (در config-server)
```properties
# Shared Public Key
shared.public-key=-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyG2RcM8Rd7T2Qc8Cl4Be
zKFgKD5kw6aDub1OJedMrZFRk/JDw+rGzX9mQBWG6uBdlI8CadhC3BRpkTQ7X+d+
eCzOi1mXTSmTIGUHw+VTzpiI5CzSPwQb1bOghutLtl7vHHFh1kcQ5SJ9SkR+GSmd
o5IxFZDDX1orIW0Mxu01guGNyKEh8MAuJZV+hfwaiQ441dMPMCyiO+FU/97BNQCo
IA1M6xPMe2MncsvHDKVBcj4fBqAw1pCtQtWVF5nWZKYQ/1z7igirUw/72/nTDpCH
65SX9a02ytx9rburr6UQq7WmnI/lcJm0+EfosQSZj/2DP/lcV7tnkZuB0rYEKw9x
oQIDAQAB
-----END PUBLIC KEY-----

# Shared Signature Configuration
shared.signature.format=username|permissions|token
shared.signature.algorithm=SHA-256
```

## تست Configuration

### 1. بررسی اتصال به Config-Server
```bash
curl http://localhost:8082/service1/signature/public-key
```

### 2. بررسی Shared Configuration
```json
{
  "success": true,
  "message": "Public key loaded from shared configuration",
  "source": "shared.properties in config-server",
  "note": "All services now use the same public key from shared configuration"
}
```

## نتیجه

### ✅ مزایای تغییر
- **Modern Approach**: استفاده از روش مدرن Spring Boot 2.4+
- **Simpler**: ساده‌تر و کمتر پیچیده
- **Future Proof**: آینده‌نگر و به‌روز
- **Best Practice**: روش استاندارد و توصیه شده

### ✅ سازگاری
- **Spring Boot 3.5.4**: کاملاً سازگار
- **Spring Cloud 2025.0.0**: پشتیبانی کامل
- **Java 21**: بدون مشکل

## خلاصه تغییرات

1. ✅ **حذف bootstrap.properties**
2. ✅ **اضافه کردن spring.config.import**
3. ✅ **انتقال تنظیمات Eureka**
4. ✅ **حذف spring-cloud-starter-bootstrap**
5. ✅ **حفظ عملکرد shared configuration**

حالا سرویس از روش مدرن Spring Boot 2.4+ استفاده می‌کند!
