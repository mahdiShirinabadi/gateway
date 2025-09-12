package com.eureka.service1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "public.keys")
public class PublicKeyConfig {
    
    private Gateway gateway = new Gateway();
    private Sso sso = new Sso();
    private Service1 service1 = new Service1();
    
    @Data
    public static class Gateway {
        private String publicKey;
        private String keyType = "RSA";
        private int keySize = 2048;
    }
    
    @Data
    public static class Sso {
        private String publicKey;
        private String keyType = "RSA";
        private int keySize = 2048;
    }
    
    @Data
    public static class Service1 {
        private String publicKey;
        private String keyType = "RSA";
        private int keySize = 2048;
    }
} 