package com.marketplace.selleridentity.identity;

import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserStatus;
import com.marketplace.selleridentity.identity.entity.VerificationToken;
import com.marketplace.selleridentity.identity.repository.RoleRepository;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import com.marketplace.selleridentity.identity.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Test
    void registerUser_shouldCreatePendingVerificationUser() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "SecurePass123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "phoneNumber": "+919876543210"
                }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

        // Verify user persisted with correct state
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getPhoneNumber()).isEqualTo("+919876543210");
        assertThat(user.getPasswordHash()).isNotEqualTo("SecurePass123"); // hashed

        // Verify CUSTOMER role assigned
        var customerRole = roleRepository.findByName(RoleType.CUSTOMER).orElseThrow();
        assertThat(userRoleRepository.existsByUserIdAndRoleId(user.getId(), customerRole.getId()))
                .isTrue();

        // Verify verification token created
        var tokens = verificationTokenRepository.findAll();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(tokens.get(0).isUsed()).isFalse();
        assertThat(tokens.get(0).getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void verifyToken_shouldActivateUser() throws Exception {
        // Setup: create user directly
        User user = User.create(
                "verify@example.com",
                "$2a$10$dummyhashvalue1234567890123456789012345678901234",
                "Jane",
                "Smith",
                "+919876543211"
        );
        user = userRepository.saveAndFlush(user);

        // Setup: create verification token
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.create(
                user,
                tokenValue,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        verificationTokenRepository.saveAndFlush(token);

        // Execute: verify the token
        mockMvc.perform(post("/api/v1/verifications/" + tokenValue))
                .andExpect(status().isNoContent());

        // Verify: token marked as used
        VerificationToken updatedToken = verificationTokenRepository.findByToken(tokenValue).orElseThrow();
        assertThat(updatedToken.isUsed()).isTrue();

        // Verify: user activated
        User updatedUser = userRepository.findByEmail("verify@example.com").orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
