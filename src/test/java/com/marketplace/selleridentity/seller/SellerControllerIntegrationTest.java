package com.marketplace.selleridentity.seller;

import com.marketplace.selleridentity.auth.security.JwtService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SellerControllerIntegrationTest {

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

    @Autowired
    private JwtService jwtService;

    @Test
    void createSeller_shouldReturn201ForAuthenticatedUser() throws Exception {
        User user = createActiveUser("seller1@example.com", "+919876540101");
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        String requestBody = """
                {
                    "displayName": "Jayesh Electronics"
                }
                """;

        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").isNotEmpty())
                .andExpect(jsonPath("$.displayName").value("Jayesh Electronics"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void createSeller_shouldReturn400WhenSellerAlreadyExists() throws Exception {
        User user = createActiveUser("seller2@example.com", "+919876540102");
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        String requestBody = """
                {
                    "displayName": "First Store"
                }
                """;

        // Create first seller
        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Attempt duplicate
        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Seller profile already exists"));
    }

    @Test
    void createSeller_shouldReturn401WithoutJwt() throws Exception {
        String requestBody = """
                {
                    "displayName": "No Auth Store"
                }
                """;

        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSeller_shouldReturn401WithInvalidJwt() throws Exception {
        String requestBody = """
                {
                    "displayName": "Bad Token Store"
                }
                """;

        mockMvc.perform(post("/api/v1/sellers")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
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
