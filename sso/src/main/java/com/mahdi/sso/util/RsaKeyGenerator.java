package com.mahdi.sso.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Log4j2
public class RsaKeyGenerator {
    
    private static final int KEY_SIZE = 2048;
    
    public KeyPair generateKeyPair() throws Exception {
        log.info("Generating RSA key pair with size: {}", KEY_SIZE);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }
    
    public PrivateKey getPrivateKey() throws Exception {
        return generateKeyPair().getPrivate();
    }
    
    public PublicKey getPublicKey() throws Exception {
        return generateKeyPair().getPublic();
    }
} 