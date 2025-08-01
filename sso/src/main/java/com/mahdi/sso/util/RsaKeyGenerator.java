package com.mahdi.sso.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Log4j2
public class RsaKeyGenerator {
    
    private static final int KEY_SIZE = 2048;
    private static final String PRIVATE_KEY_FILE = "rsa_private.key";
    private static final String PUBLIC_KEY_FILE = "rsa_public.key";
    private static final String KEY_STORE_FILE = "keystore.p12";
    private static final String KEY_ALIAS = "jwt-key";
    private static final String KEY_STORE_PASSWORD = "changeit";
    
    private KeyPair keyPair;
    
    public RsaKeyGenerator() {
        loadOrGenerateKeys();
    }
    
    private void loadOrGenerateKeys() {
        try {
            // Try to load existing keys from resources
            if (loadKeysFromResources()) {
                log.info("RSA keys loaded from resources successfully");
                return;
            }
            
            // Try to load from file system
            if (loadKeysFromFileSystem()) {
                log.info("RSA keys loaded from file system successfully");
                return;
            }
            
            // Generate new keys if none exist
            log.info("No existing RSA keys found, generating new ones...");
            generateAndSaveKeys();
            
        } catch (Exception e) {
            log.error("Error loading/generating RSA keys: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize RSA keys", e);
        }
    }
    
    private boolean loadKeysFromResources() {
        try {
            ClassPathResource privateKeyResource = new ClassPathResource(PRIVATE_KEY_FILE);
            ClassPathResource publicKeyResource = new ClassPathResource(PUBLIC_KEY_FILE);
            
            if (privateKeyResource.exists() && publicKeyResource.exists()) {
                byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyResource.getURI()));
                byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyResource.getURI()));
                
                PrivateKey privateKey = loadPrivateKey(privateKeyBytes);
                PublicKey publicKey = loadPublicKey(publicKeyBytes);
                
                this.keyPair = new KeyPair(publicKey, privateKey);
                return true;
            }
        } catch (Exception e) {
            log.warn("Could not load keys from resources: {}", e.getMessage());
        }
        return false;
    }
    
    private boolean loadKeysFromFileSystem() {
        try {
            Path privateKeyPath = Paths.get(PRIVATE_KEY_FILE);
            Path publicKeyPath = Paths.get(PUBLIC_KEY_FILE);
            
            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
                byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
                
                PrivateKey privateKey = loadPrivateKey(privateKeyBytes);
                PublicKey publicKey = loadPublicKey(publicKeyBytes);
                
                this.keyPair = new KeyPair(publicKey, privateKey);
                return true;
            }
        } catch (Exception e) {
            log.warn("Could not load keys from file system: {}", e.getMessage());
        }
        return false;
    }
    
    private void generateAndSaveKeys() throws Exception {
        log.info("Generating new RSA key pair with size: {}", KEY_SIZE);
        
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        this.keyPair = keyPairGenerator.generateKeyPair();
        
        // Save keys to file system
        saveKeysToFileSystem();
        
        log.info("RSA keys generated and saved successfully");
    }
    
    private void saveKeysToFileSystem() throws Exception {
        // Save private key
        byte[] privateKeyBytes = this.keyPair.getPrivate().getEncoded();
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(privateKeyBytes) +
                "\n-----END PRIVATE KEY-----";
        
        Files.write(Paths.get(PRIVATE_KEY_FILE), privateKeyPEM.getBytes());
        
        // Save public key
        byte[] publicKeyBytes = this.keyPair.getPublic().getEncoded();
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(publicKeyBytes) +
                "\n-----END PUBLIC KEY-----";
        
        Files.write(Paths.get(PUBLIC_KEY_FILE), publicKeyPEM.getBytes());
        
        log.info("RSA keys saved to: {} and {}", PRIVATE_KEY_FILE, PUBLIC_KEY_FILE);
    }
    
    private PrivateKey loadPrivateKey(byte[] keyBytes) throws Exception {
        String keyString = new String(keyBytes);
        String privateKeyPEM = keyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    
    private PublicKey loadPublicKey(byte[] keyBytes) throws Exception {
        String keyString = new String(keyBytes);
        String publicKeyPEM = keyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    
    public KeyPair getKeyPair() {
        return this.keyPair;
    }
    
    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }
    
    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }
    
    public String getPublicKeyAsString() {
        try {
            byte[] publicKeyBytes = this.keyPair.getPublic().getEncoded();
            return Base64.getEncoder().encodeToString(publicKeyBytes);
        } catch (Exception e) {
            log.error("Error encoding public key: {}", e.getMessage());
            return null;
        }
    }
} 