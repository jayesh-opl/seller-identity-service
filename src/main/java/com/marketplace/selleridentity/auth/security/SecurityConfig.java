package com.marketplace.selleridentity.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary security configuration for development phase.
 *
 * <p>Permits all requests without authentication to enable:
 * <ul>
 *   <li>Local development and manual API testing</li>
 *   <li>Swagger UI access at /swagger-ui/**</li>
 *   <li>Actuator health/metrics access</li>
 * </ul>
 *
 * <p><strong>TODO:</strong> Replace with JWT-based security when authentication
 * layer is implemented. This class will be refactored to:
 * <ul>
 *   <li>Validate JWT tokens on protected endpoints</li>
 *   <li>Enforce role-based access control</li>
 *   <li>Keep Swagger/actuator publicly accessible or behind admin role</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .build();
    }
}
