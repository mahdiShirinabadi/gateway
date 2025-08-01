# SSO Service

این سرویس برای مدیریت احراز هویت و تولید توکن JWT با الگوریتم RSA طراحی شده است.

## پیش‌نیازها

- Java 21
- Maven
- PostgreSQL

## تنظیمات دیتابیس

1. دیتابیس PostgreSQL را نصب کنید
2. دیتابیس `sso_db` را ایجاد کنید
3. تنظیمات اتصال به دیتابیس را در `application.properties` بروزرسانی کنید

## اجرای پروژه

```bash
mvn spring-boot:run
```

سرویس روی پورت 8081 اجرا می‌شود.

## API Endpoints

### 1. ورود کاربر
**POST** `/api/auth/login`

**Request Body:**
```json
{
    "username": "testuser",
    "password": "password123"
}
```

**Response (Success):**
```json
{
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "message": "Login successful",
    "success": true
}
```

**Response (Error):**
```json
{
    "message": "Invalid password",
    "success": false
}
```

### 2. اعتبارسنجی توکن
**POST** `/api/auth/validate?token={token}&username={username}`

**Response:**
```json
true
```

### 3. دریافت کلید عمومی RSA
**GET** `/api/auth/public-key`

**Response:**
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
```

### 4. بررسی وضعیت سرویس
**GET** `/api/auth/health`

**Response:**
```
SSO Service is running
```

## کاربر تست

پس از اجرای پروژه، یک کاربر تست با مشخصات زیر ایجاد می‌شود:
- **Username:** testuser
- **Password:** password123

## تنظیمات JWT (RSA)

- **Algorithm:** RS256 (RSA with SHA-256)
- **Key Size:** 2048 bits
- **Expiration:** 24 ساعت (86400000 میلی‌ثانیه)
- **Private Key:** برای امضای توکن‌ها
- **Public Key:** برای اعتبارسنجی توکن‌ها

## مزایای استفاده از RSA

1. **امنیت بالاتر:** RSA از کلیدهای نامتقارن استفاده می‌کند
2. **اعتبارسنجی خارجی:** سرویس‌های دیگر می‌توانند با کلید عمومی توکن‌ها را اعتبارسنجی کنند
3. **عدم نیاز به اشتراک کلید:** هر سرویس فقط به کلید عمومی نیاز دارد

## استفاده از Lombok

این پروژه از Lombok برای کاهش boilerplate code استفاده می‌کند:

- **@Data:** تولید getters، setters، toString، equals و hashCode
- **@NoArgsConstructor:** تولید constructor بدون پارامتر
- **@AllArgsConstructor:** تولید constructor با تمام پارامترها
- **@RequiredArgsConstructor:** تولید constructor برای فیلدهای final
- **@Log4j2:** تولید logger برای کلاس

## Logging با Log4j2

این پروژه از Log4j2 برای logging استفاده می‌کند:

### ویژگی‌های Log4j2:
- **Performance بالا:** Log4j2 سریع‌تر از Logback و Log4j1 است
- **Async Logging:** برای بهبود عملکرد
- **Rolling Files:** فایل‌های لاگ به صورت خودکار rotate می‌شوند
- **Separate Error Logs:** لاگ‌های خطا در فایل جداگانه ذخیره می‌شوند
- **Properties Configuration:** تنظیمات با فایل properties به جای XML

### فایل‌های لاگ:
- `logs/sso.log` - لاگ‌های عمومی
- `logs/sso-error.log` - لاگ‌های خطا

### سطوح لاگ:
- **DEBUG:** برای اطلاعات دقیق و troubleshooting
- **INFO:** برای اطلاعات عمومی
- **WARN:** برای هشدارها
- **ERROR:** برای خطاها

### تنظیمات لاگ:
- Console output با رنگ‌بندی
- File output با timestamp
- Rolling policy (10MB per file, max 10 files)
- Async appenders برای عملکرد بهتر
- Properties-based configuration (`log4j2.properties`)

### مزایای Properties Configuration:
- **خوانایی بهتر:** فرمت properties ساده‌تر و قابل فهم‌تر است
- **مدیریت آسان‌تر:** تغییر تنظیمات بدون نیاز به XML syntax
- **کمتر verbose:** کد کمتر و خواناتر
- **سازگاری بهتر:** با سایر فایل‌های properties

## نکات امنیتی

1. کلید خصوصی فقط در سرویس SSO نگهداری می‌شود
2. رمزهای عبور با BCrypt رمزگذاری می‌شوند
3. توکن‌ها با الگوریتم RS256 امضا می‌شوند
4. کلیدهای RSA در هر بار راه‌اندازی مجدد تولید می‌شوند (در محیط تولید باید کلیدها را ذخیره کنید)

---

# SSO Service

This service is designed for authentication management and JWT token generation using RSA algorithm.

## Prerequisites

- Java 21
- Maven
- PostgreSQL

## Database Configuration

1. Install PostgreSQL database
2. Create database `sso_db`
3. Update database connection settings in `application.properties`

## Running the Project

```bash
mvn spring-boot:run
```

The service runs on port 8081.

## API Endpoints

### 1. User Login
**POST** `/api/auth/login`

**Request Body:**
```json
{
    "username": "testuser",
    "password": "password123"
}
```

**Response (Success):**
```json
{
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "message": "Login successful",
    "success": true
}
```

**Response (Error):**
```json
{
    "message": "Invalid password",
    "success": false
}
```

### 2. Token Validation
**POST** `/api/auth/validate?token={token}&username={username}`

**Response:**
```json
true
```

### 3. Get RSA Public Key
**GET** `/api/auth/public-key`

**Response:**
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
```

### 4. Health Check
**GET** `/api/auth/health`

**Response:**
```
SSO Service is running
```

## Test User

After running the project, a test user is created with the following credentials:
- **Username:** testuser
- **Password:** password123

## JWT Configuration (RSA)

- **Algorithm:** RS256 (RSA with SHA-256)
- **Key Size:** 2048 bits
- **Expiration:** 24 hours (86400000 milliseconds)
- **Private Key:** For signing tokens
- **Public Key:** For token validation

## Benefits of Using RSA

1. **Higher Security:** RSA uses asymmetric keys
2. **External Validation:** Other services can validate tokens using the public key
3. **No Key Sharing:** Each service only needs the public key

## Using Lombok

This project uses Lombok to reduce boilerplate code:

- **@Data:** Generates getters, setters, toString, equals and hashCode
- **@NoArgsConstructor:** Generates no-args constructor
- **@AllArgsConstructor:** Generates constructor with all parameters
- **@RequiredArgsConstructor:** Generates constructor for final fields
- **@Log4j2:** Generates logger for the class

## Logging with Log4j2

This project uses Log4j2 for logging:

### Log4j2 Features:
- **High Performance:** Log4j2 is faster than Logback and Log4j1
- **Async Logging:** For better performance
- **Rolling Files:** Log files are automatically rotated
- **Separate Error Logs:** Error logs are stored in separate files
- **Properties Configuration:** Configuration using properties file instead of XML

### Log Files:
- `logs/sso.log` - General logs
- `logs/sso-error.log` - Error logs

### Log Levels:
- **DEBUG:** For detailed information and troubleshooting
- **INFO:** For general information
- **WARN:** For warnings
- **ERROR:** For errors

### Log Configuration:
- Console output with color coding
- File output with timestamp
- Rolling policy (10MB per file, max 10 files)
- Async appenders for better performance
- Properties-based configuration (`log4j2.properties`)

### Benefits of Properties Configuration:
- **Better Readability:** Properties format is simpler and more understandable
- **Easier Management:** Change settings without XML syntax
- **Less Verbose:** Less code and more readable
- **Better Compatibility:** With other properties files

## Security Notes

1. Private key is only stored in the SSO service
2. Passwords are encrypted with BCrypt
3. Tokens are signed with RS256 algorithm
4. RSA keys are generated on each restart (in production, keys should be stored) 