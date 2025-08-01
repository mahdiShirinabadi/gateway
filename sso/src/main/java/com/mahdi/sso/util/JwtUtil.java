package com.mahdi.sso.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtUtil {
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private final RsaKeyGenerator rsaKeyGenerator;
    
    private KeyPair keyPair;
    
    @PostConstruct
    public void init() {
        try {
            this.keyPair = rsaKeyGenerator.generateKeyPair();
            log.info("RSA key pair generated successfully");
        } catch (Exception e) {
            log.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
    
    private PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
    
    private PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
    
    public String generateToken(String username) {
        log.debug("Generating JWT token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
    
    public Boolean validateToken(String token, String username) {
        log.debug("Validating JWT token for user: {}", username);
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // Method to get public key for external validation
    public PublicKey getPublicKeyForValidation() {
        return getPublicKey();
    }
} 