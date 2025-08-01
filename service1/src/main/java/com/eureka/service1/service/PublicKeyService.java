package com.eureka.service1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicKeyService {
    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${gateway.service.url:http://localhost:8080/api/gateway/public-key}")
    private String gatewayServiceUrl;
    
    private static final String PUBLIC_KEY_CACHE_PREFIX = "public_key:";
    private static final String PUBLIC_KEY_CACHE_KEY = "gateway_public_key";
    private static final Duration PUBLIC_KEY_CACHE_TTL = Duration.ofHours(24);

    public PublicKey getGatewayPublicKey() {
        try {
            // ابتدا از Redis بررسی کن
            PublicKey cachedKey = getCachedPublicKey();
            if (cachedKey != null) {
                log.info("Service1: کلید عمومی از کش Redis دریافت شد");
                return cachedKey;
            }

            // اگر در کش نبود، از Gateway دریافت کن
            log.info("Service1: دریافت کلید عمومی از Gateway");
            PublicKey publicKey = fetchPublicKeyFromGateway();
            if (publicKey != null) {
                cachePublicKey(publicKey);
                log.info("Service1: کلید عمومی در کش Redis ذخیره شد");
            }
            return publicKey;
            
        } catch (Exception e) {
            log.error("Service1: خطا در دریافت کلید عمومی از Gateway: {}", e.getMessage());
            return null;
        }
    }

    private PublicKey fetchPublicKeyFromGateway() {
        try {
            log.info("Service1: درخواست کلید عمومی به Gateway: {}", gatewayServiceUrl);
            
            Map<String, Object> response = webClient.get()
                    .uri(gatewayServiceUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("success") && (Boolean) response.get("success")) {
                String publicKeyString = (String) response.get("publicKey");
                if (publicKeyString != null && !publicKeyString.isEmpty()) {
                    log.info("Service1: کلید عمومی با موفقیت از Gateway دریافت شد");
                    return decodePublicKey(publicKeyString);
                } else {
                    log.error("Service1: کلید عمومی در پاسخ Gateway موجود نیست");
                }
            } else {
                log.error("Service1: پاسخ ناموفق از Gateway: {}", response);
            }
            
        } catch (Exception e) {
            log.error("Service1: خطا در دریافت کلید عمومی از Gateway: {}", e.getMessage());
        }
        
        return null;
    }

    private PublicKey decodePublicKey(String publicKeyString) {
        try {
            // حذف header و footer اگر وجود دارد
            String cleanKey = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
            
        } catch (Exception e) {
            log.error("Service1: خطا در رمزگشایی کلید عمومی: {}", e.getMessage());
            return null;
        }
    }

    private PublicKey getCachedPublicKey() {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + PUBLIC_KEY_CACHE_KEY;
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                String publicKeyString = (String) cachedValue;
                return decodePublicKey(publicKeyString);
            }
            
        } catch (Exception e) {
            log.error("Service1: خطا در دریافت کلید عمومی از کش: {}", e.getMessage());
        }
        
        return null;
    }

    private void cachePublicKey(PublicKey publicKey) {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + PUBLIC_KEY_CACHE_KEY;
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            redisTemplate.opsForValue().set(cacheKey, publicKeyString, PUBLIC_KEY_CACHE_TTL);
            log.info("Service1: کلید عمومی در کش Redis ذخیره شد با TTL: {}", PUBLIC_KEY_CACHE_TTL);
            
        } catch (Exception e) {
            log.error("Service1: خطا در ذخیره کلید عمومی در کش: {}", e.getMessage());
        }
    }

    public void invalidateCachedPublicKey() {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + PUBLIC_KEY_CACHE_KEY;
            redisTemplate.delete(cacheKey);
            log.info("Service1: کلید عمومی از کش Redis حذف شد");
            
        } catch (Exception e) {
            log.error("Service1: خطا در حذف کلید عمومی از کش: {}", e.getMessage());
        }
    }

    public boolean hasCachedPublicKey() {
        try {
            String cacheKey = PUBLIC_KEY_CACHE_PREFIX + PUBLIC_KEY_CACHE_KEY;
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
            
        } catch (Exception e) {
            log.error("Service1: خطا در بررسی وجود کلید عمومی در کش: {}", e.getMessage());
            return false;
        }
    }

    public PublicKey refreshPublicKey() {
        log.info("Service1: به‌روزرسانی کلید عمومی از Gateway");
        invalidateCachedPublicKey();
        return getGatewayPublicKey();
    }
} 