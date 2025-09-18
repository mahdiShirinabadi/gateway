package com.eureka.service1.service;

import com.eureka.service1.config.SignatureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SignatureService {
    
    private final SignatureConfig signatureConfig;
    
    /**
     * Generate signature for token data using configured format
     */
    public String generateSignature(String username, List<String> permissions, String token) {
        try {
            String dataToSign = buildSignatureData(username, permissions, token);
            
            if (signatureConfig.isDebugEnabled()) {
                log.debug("Generating signature for data: {}", dataToSign);
            }
            
            MessageDigest digest = MessageDigest.getInstance(signatureConfig.getAlgorithm());
            byte[] hash = digest.digest(dataToSign.getBytes(StandardCharsets.UTF_8));
            
            String signature = bytesToHex(hash);
            
            if (signatureConfig.isDebugEnabled()) {
                log.debug("Generated signature: {}", signature);
            }
            
            return signature;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating signature: {}", e.getMessage());
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
    
    /**
     * Verify signature for token data
     */
    public boolean verifySignature(String username, List<String> permissions, String token, String expectedSignature) {
        try {
            String generatedSignature = generateSignature(username, permissions, token);
            boolean isValid = generatedSignature.equals(expectedSignature);
            
            if (signatureConfig.isDebugEnabled()) {
                log.debug("Signature verification: {} (expected: {}, generated: {})", 
                         isValid, expectedSignature, generatedSignature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Build signature data according to configured format
     */
    private String buildSignatureData(String username, List<String> permissions, String token) {
        String sortedPermissions = permissions != null ? 
            permissions.stream().sorted().collect(java.util.stream.Collectors.joining(",")) : "";
        
        // Use configured format: username|permissions|token
        return String.format("%s%s%s%s%s", 
            username, 
            signatureConfig.getSeparator(),
            sortedPermissions,
            signatureConfig.getSeparator(),
            token
        );
    }
    
    /**
     * Get the signature format for debugging
     */
    public String getSignatureFormat() {
        return signatureConfig.getFormat();
    }
    
    /**
     * Get the signature fields for debugging
     */
    public List<String> getSignatureFields() {
        return signatureConfig.getSignatureFields();
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
