package com.eureka.gateway.util;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class GatewayKeyGenerator {
    private static final String KEYS_DIR = "gateway-keys";
    private static final String PRIVATE_KEY_FILE = "gateway-private.key";
    private static final String PUBLIC_KEY_FILE = "gateway-public.key";
    
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void initializeKeys() {
        try {
            // Create keys directory if it doesn't exist
            createKeysDirectory();
            
            // Try to load existing keys
            if (loadExistingKeys()) {
                System.out.println("Gateway: کلیدهای موجود بارگذاری شدند");
            } else {
                // Generate new keys
                generateNewKeys();
                saveKeys();
                System.out.println("Gateway: کلیدهای جدید تولید و ذخیره شدند");
            }
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در مقداردهی اولیه کلیدها: " + e.getMessage());
        }
    }

    private void createKeysDirectory() throws IOException {
        Path keysPath = Paths.get(KEYS_DIR);
        if (!Files.exists(keysPath)) {
            Files.createDirectories(keysPath);
            System.out.println("Gateway: پوشه کلیدها ایجاد شد: " + keysPath.toAbsolutePath());
        }
    }

    private boolean loadExistingKeys() {
        try {
            File privateKeyFile = new File(KEYS_DIR, PRIVATE_KEY_FILE);
            File publicKeyFile = new File(KEYS_DIR, PUBLIC_KEY_FILE);
            
            if (privateKeyFile.exists() && publicKeyFile.exists()) {
                // Load private key
                byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privateKey = keyFactory.generatePrivate(privateKeySpec);
                
                // Load public key
                byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                publicKey = keyFactory.generatePublic(publicKeySpec);
                
                System.out.println("Gateway: کلیدهای موجود با موفقیت بارگذاری شدند");
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Gateway: خطا در بارگذاری کلیدهای موجود: " + e.getMessage());
        }
        
        return false;
    }

    private void generateNewKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        
        System.out.println("Gateway: کلیدهای جدید تولید شدند");
    }

    private void saveKeys() throws IOException {
        // Save private key
        File privateKeyFile = new File(KEYS_DIR, PRIVATE_KEY_FILE);
        try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
            fos.write(privateKey.getEncoded());
        }
        
        // Save public key
        File publicKeyFile = new File(KEYS_DIR, PUBLIC_KEY_FILE);
        try (FileOutputStream fos = new FileOutputStream(publicKeyFile)) {
            fos.write(publicKey.getEncoded());
        }
        
        System.out.println("Gateway: کلیدها در فایل‌ها ذخیره شدند");
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyAsString() {
        if (publicKey != null) {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }
        return null;
    }

    public String getPrivateKeyAsString() {
        if (privateKey != null) {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }
        return null;
    }

    public boolean areKeysAvailable() {
        return privateKey != null && publicKey != null;
    }

    public void regenerateKeys() {
        try {
            generateNewKeys();
            saveKeys();
            System.out.println("Gateway: کلیدها مجدداً تولید و ذخیره شدند");
        } catch (Exception e) {
            System.err.println("Gateway: خطا در تولید مجدد کلیدها: " + e.getMessage());
        }
    }
} 