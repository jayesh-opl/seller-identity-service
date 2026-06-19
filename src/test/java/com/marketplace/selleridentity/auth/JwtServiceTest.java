package com.marketplace.selleridentity.auth;

import com.marketplace.selleridentity.auth.security.JwtProperties;
import com.marketplace.selleridentity.auth.security.JwtService;
import com.marketplace.selleridentity.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "this-is-a-test-secret-that-is-at-least-32-bytes-long";
    private static final long EXPIRATION_SECONDS = 3600;

    private JwtService jwtService;
    private SecretKey verificationKey;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, EXPIRATION_SECONDS);
        jwtService = new JwtService(properties);
        verificationKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateToken_shouldReturnSignedJwt() throws Exception {
        User user = createUserWithId("test@example.com", "+919876543210");

        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_shouldContainExpectedSubject() throws Exception {
        User user = createUserWithId("jwt@example.com", "+919876543211");

        String token = jwtService.generateToken(user, List.of("SELLER"));

        Claims claims = Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("jwt@example.com");
        assertThat(claims.get("status", String.class)).isEqualTo("PENDING_VERIFICATION");
        assertThat(claims.get("roles", List.class)).containsExactly("SELLER");
    }

    @Test
    void generateToken_shouldSetExpiration() throws Exception {
        User user = createUserWithId("exp@example.com", "+919876543212");

        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        Claims claims = Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getExpiration()).isAfter(new Date());
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
    }

    @Test
    void constructor_shouldFailIfSecretTooShort() {
        JwtProperties shortSecret = new JwtProperties("short", 3600);

        assertThatThrownBy(() -> new JwtService(shortSecret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    private User createUserWithId(String email, String phone) throws Exception {
        User user = User.create(email, "hashedpw", "John", "Doe", phone);
        Field idField = user.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, UUID.randomUUID());
        return user;
    }
}
