package com.eureka.service1.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final WebClient webClient;
    
    @Value("${acl.service.url}")
    private String aclServiceUrl;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        try {
            List<String> permissions = getUserPermissions(username);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
            return User.builder()
                    .username(username)
                    .password("")
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (Exception e) {
            log.error("Error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
    
    private List<String> getUserPermissions(String username) {
        try {
            log.debug("Fetching permissions for user: {} from ACL service", username);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(aclServiceUrl.replace("/check", "/user-permissions") + "?username=" + username)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("permissions")) {
                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) response.get("permissions");
                log.debug("Retrieved {} permissions for user: {}", permissions.size(), username);
                return permissions;
            } else {
                log.warn("No permissions found for user: {}", username);
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error fetching permissions for user {}: {}", username, e.getMessage());
            return new ArrayList<>();
        }
    }
} 