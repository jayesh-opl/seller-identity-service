package com.marketplace.selleridentity.auth.security;

import com.marketplace.selleridentity.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Generates and validates signed JWT access tokens.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes for HS256. Current length: " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = jwtProperties.expirationSeconds();
    }

    /**
     * Generates a signed JWT for the given user.
     */
    public String generateToken(User user, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("status", user.getStatus().name())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (user ID) from a valid token.
     */
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Validates the token signature and expiration.
     *
     * @return true if the token is well-formed, correctly signed, and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses and validates the token, returning all claims.
     *
     * @throws JwtException if token is invalid, expired, or malformed
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
