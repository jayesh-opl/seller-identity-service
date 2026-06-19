package com.marketplace.selleridentity.auth;

import com.marketplace.selleridentity.auth.security.JwtProperties;
import com.marketplace.selleridentity.auth.security.JwtService;
import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserRole;
import com.marketplace.selleridentity.identity.repository.RoleRepository;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class JwtProtectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void me_shouldReturnUserInfoWithValidJwt() throws Exception {
        User user = createActiveUser("jwt-me@example.com", "+919876549001");
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.subject").value(user.getId().toString()));
    }

    @Test
    void me_shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn401WithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn401WithExpiredJwt() throws Exception {
        // Build a token that expired 1 hour ago
        String expiredToken = Jwts.builder()
                .subject("some-user-id")
                .claim("email", "expired@example.com")
                .claim("status", "ACTIVE")
                .claim("roles", List.of("CUSTOMER"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
                .expiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(Keys.hmacShaKeyFor(
                        jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    private User createActiveUser(String email, String phone) {
        User user = User.create(email, passwordEncoder.encode("pass"), "Test", "User", phone);
        user = userRepository.saveAndFlush(user);
        user.activate();
        user = userRepository.saveAndFlush(user);

        var role = roleRepository.findByName(RoleType.CUSTOMER).orElseThrow();
        userRoleRepository.saveAndFlush(UserRole.assign(user, role));
        return user;
    }
}
