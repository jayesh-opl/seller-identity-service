package com.marketplace.selleridentity.auth;

import com.marketplace.selleridentity.identity.entity.Role;
import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserRole;
import com.marketplace.selleridentity.identity.repository.RoleRepository;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_shouldReturnJwtForActiveUser() throws Exception {
        createActiveUser("login@example.com", "SecurePass123", "+919876540001");

        String requestBody = """
                {
                    "email": "login@example.com",
                    "password": "SecurePass123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void login_shouldReturn400ForWrongPassword() throws Exception {
        createActiveUser("wrong@example.com", "CorrectPass123", "+919876540002");

        String requestBody = """
                {
                    "email": "wrong@example.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturnErrorForInactiveUser() throws Exception {
        // Create user but don't activate
        User user = User.create(
                "inactive@example.com",
                passwordEncoder.encode("SecurePass123"),
                "Test", "User", "+919876540003"
        );
        userRepository.saveAndFlush(user);

        String requestBody = """
                {
                    "email": "inactive@example.com",
                    "password": "SecurePass123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("User account is not active"));
    }

    @Test
    void login_shouldReturn400ForInvalidEmailFormat() throws Exception {
        String requestBody = """
                {
                    "email": "not-an-email",
                    "password": "SecurePass123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400ForBlankPassword() throws Exception {
        String requestBody = """
                {
                    "email": "user@example.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private void createActiveUser(String email, String rawPassword, String phone) {
        User user = User.create(
                email,
                passwordEncoder.encode(rawPassword),
                "Test", "User", phone
        );
        user = userRepository.saveAndFlush(user);
        user.activate();
        userRepository.saveAndFlush(user);

        Role role = roleRepository.findByName(RoleType.CUSTOMER).orElseThrow();
        userRoleRepository.saveAndFlush(UserRole.assign(user, role));
    }
}
