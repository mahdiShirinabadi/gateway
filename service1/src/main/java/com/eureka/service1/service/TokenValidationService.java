package com.eureka.service1.service;

import com.eureka.service1.model.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Log4j2
public class TokenValidationService {

    private final TokenInfoService tokenInfoService;
    private final PublicKey publicKey;

    @Value("${jwt.public-key}")
    private String publicKeyString;

    public TokenValidationService(TokenInfoService tokenInfoService) {
        this.tokenInfoService = tokenInfoService;
        this.publicKey = loadPublicKey();
    }

    private PublicKey loadPublicKey() {
        try {
            // Remove headers and decode
            String publicKeyPEM = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error loading public key: {}", e.getMessage());
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public boolean validateToken(String token, String userId) {
        try {
            log.info("Validating token in Service1 for user: {}", userId);
            
            // First check if token exists in Redis with JSON data
            TokenInfo tokenInfo = tokenInfoService.getTokenInfo(token);
            
            if (tokenInfo == null) {
                log.warn("Token not found in Redis cache");
                return false;
            }
            
            // Verify the username matches
            if (!userId.equals(tokenInfo.getUsername())) {
                log.warn("Token username mismatch. Expected: {}, Found: {}", userId, tokenInfo.getUsername());
                return false;
            }
            
            // Check if token is expired
            if (tokenInfo.isExpired()) {
                log.warn("Token has expired");
                tokenInfoService.removeToken(token); // Clean up expired token
                return false;
            }
            
            log.info("Token validated successfully for user: {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }


    public String getUsernameFromToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
            
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
            
            Claims claims = parser.parseSignedClaims(token).getPayload();

            if (claims == null) {
                return true; // Invalid token is considered expired
            }

            // Check JWT expiration
            return claims.getExpiration() != null && 
                   claims.getExpiration().before(new java.util.Date());

        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // If we can't parse, consider it expired
        }
    }

    public boolean hasPermission(String token, String requiredPermission) {
        try {
            // Use the new TokenInfoService to check permission
            boolean hasPermission = tokenInfoService.hasPermission(token, requiredPermission);
            log.info("Permission check result: {} for permission: {}", hasPermission, requiredPermission);
            return hasPermission;
        } catch (Exception e) {
            log.error("Error checking permissions: {}", e.getMessage());
            return false;
        }
    }
}
