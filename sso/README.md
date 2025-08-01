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

## نکات امنیتی

1. کلید خصوصی فقط در سرویس SSO نگهداری می‌شود
2. رمزهای عبور با BCrypt رمزگذاری می‌شوند
3. توکن‌ها با الگوریتم RS256 امضا می‌شوند
4. کلیدهای RSA در هر بار راه‌اندازی مجدد تولید می‌شوند (در محیط تولید باید کلیدها را ذخیره کنید) 