package com.mahdi.sso.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Utility class for generating RSA key pairs
 */
public class RsaKeyGenerator {
    
    public static void main(String[] args) {
        try {
            // Generate SSO keys
            KeyPair ssoKeyPair = generateKeyPair();
            System.out.println("=== SSO Keys ===");
            System.out.println("SSO Private Key:");
            System.out.println(formatPrivateKey(ssoKeyPair.getPrivate()));
            System.out.println("\nSSO Public Key:");
            System.out.println(formatPublicKey(ssoKeyPair.getPublic()));
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // Generate Gateway keys
            KeyPair gatewayKeyPair = generateKeyPair();
            System.out.println("=== Gateway Keys ===");
            System.out.println("Gateway Private Key:");
            System.out.println(formatPrivateKey(gatewayKeyPair.getPrivate()));
            System.out.println("\nGateway Public Key:");
            System.out.println(formatPublicKey(gatewayKeyPair.getPublic()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
    
    private static String formatPrivateKey(PrivateKey privateKey) {
        String base64Key = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
               insertLineBreaks(base64Key) +
               "\n-----END PRIVATE KEY-----";
    }
    
    private static String formatPublicKey(PublicKey publicKey) {
        String base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
               insertLineBreaks(base64Key) +
               "\n-----END PUBLIC KEY-----";
    }
    
    private static String insertLineBreaks(String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key.length(); i += 64) {
            result.append(key, i, Math.min(i + 64, key.length()));
            if (i + 64 < key.length()) {
                result.append("\n");
            }
        }
        return result.toString();
    }
}