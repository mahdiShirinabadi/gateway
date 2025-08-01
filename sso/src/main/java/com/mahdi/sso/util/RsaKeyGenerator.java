package com.mahdi.sso.util;

import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class RsaKeyGenerator {
    
    private static final int KEY_SIZE = 2048;
    
    public KeyPair generateKeyPair() throws Exception {
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