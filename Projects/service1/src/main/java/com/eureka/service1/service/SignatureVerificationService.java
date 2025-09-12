package com.eureka.service1.service;

import com.eureka.service1.model.SignedTokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureVerificationService {
    private final PublicKeyService publicKeyService;

    /**
     * Verify signature of token using Gateway public key from Config Server
     */
    public boolean verifyGatewaySignature(SignedTokenData signedTokenData) {
        try {
            PublicKey publicKey = publicKeyService.getGatewayPublicKey();
            if (publicKey == null) {
                log.error("Service1: Gateway public key not available from Config Server");
                return false;
            }

            String dataToVerify = signedTokenData.getSignedData();
            String signature = signedTokenData.getSignature();

            boolean isValid = verifySignatureWithPublicKey(dataToVerify, signature, publicKey);
            
            if (isValid) {
                log.info("Service1: Token signature verified with Gateway public key from Config Server");
            } else {
                log.warn("Service1: Token signature verification failed with Gateway public key");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Service1: Error verifying Gateway signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify signature using SSO public key from Config Server
     */
    public boolean verifySsoSignature(SignedTokenData signedTokenData) {
        try {
            PublicKey publicKey = publicKeyService.getSsoPublicKey();
            if (publicKey == null) {
                log.error("Service1: SSO public key not available from Config Server");
                return false;
            }

            String dataToVerify = signedTokenData.getSignedData();
            String signature = signedTokenData.getSignature();

            boolean isValid = verifySignatureWithPublicKey(dataToVerify, signature, publicKey);
            
            if (isValid) {
                log.info("Service1: Token signature verified with SSO public key from Config Server");
            } else {
                log.warn("Service1: Token signature verification failed with SSO public key");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Service1: Error verifying SSO signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify signature using RSA public key
     */
    private boolean verifySignatureWithPublicKey(String data, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            boolean isValid = sig.verify(signatureBytes);
            
            log.debug("Service1: RSA signature verification: {}", isValid);
            return isValid;
            
        } catch (SignatureException e) {
            log.error("Service1: Error verifying RSA signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Service1: Error verifying RSA signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify signature using hash (fallback method)
     */
    public boolean verifySignatureWithHash(SignedTokenData signedTokenData) {
        try {
            String expectedSignature = generateHash(signedTokenData.getSignedData());
            String actualSignature = signedTokenData.getSignature();
            
            boolean isValid = expectedSignature.equals(actualSignature);
            
            if (isValid) {
                log.info("Service1: Token signature verified with hash");
            } else {
                log.warn("Service1: Token signature verification failed with hash");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Service1: Error verifying hash signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate hash from data
     */
    private String generateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Service1: Error generating hash: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Comprehensive signature verification with fallback
     */
    public boolean verifySignatureComprehensive(SignedTokenData signedTokenData, String serviceName) {
        // First try with the specified service's public key
        boolean rsaValid = false;
        
        switch (serviceName.toLowerCase()) {
            case "gateway":
                rsaValid = verifyGatewaySignature(signedTokenData);
                break;
            case "sso":
                rsaValid = verifySsoSignature(signedTokenData);
                break;
            default:
                log.warn("Service1: Unknown service for signature verification: {}", serviceName);
        }
        
        if (rsaValid) {
            return true;
        }

        // If RSA fails, try with hash
        log.warn("Service1: RSA verification failed for {}, trying hash", serviceName);
        return verifySignatureWithHash(signedTokenData);
    }

    /**
     * Check if public keys are available from Config Server
     */
    public boolean isGatewayPublicKeyAvailable() {
        return publicKeyService.hasCachedPublicKey("gateway") || 
               publicKeyService.getGatewayPublicKey() != null;
    }

    public boolean isSsoPublicKeyAvailable() {
        return publicKeyService.hasCachedPublicKey("sso") || 
               publicKeyService.getSsoPublicKey() != null;
    }

    /**
     * Refresh public keys from Config Server
     */
    public void refreshGatewayPublicKey() {
        log.info("Service1: Refreshing Gateway public key from Config Server");
        publicKeyService.refreshPublicKey("gateway");
    }

    public void refreshSsoPublicKey() {
        log.info("Service1: Refreshing SSO public key from Config Server");
        publicKeyService.refreshPublicKey("sso");
    }
} 