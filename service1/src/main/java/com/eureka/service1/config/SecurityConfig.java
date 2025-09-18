package com.eureka.service1.config;

import com.eureka.service1.filter.Service1SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final Service1SecurityFilter service1SecurityFilter;

    public SecurityConfig(Service1SecurityFilter service1SecurityFilter) {
        this.service1SecurityFilter = service1SecurityFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/service1/actuator/**").permitAll() // Allow actuator endpoints
                .requestMatchers("/service1/public/**").permitAll() // Allow public endpoints
                .anyRequest().authenticated()
            )
            .addFilterBefore(service1SecurityFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
} 