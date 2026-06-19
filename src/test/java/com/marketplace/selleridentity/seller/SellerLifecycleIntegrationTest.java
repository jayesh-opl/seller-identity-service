package com.marketplace.selleridentity.seller;

import com.marketplace.selleridentity.auth.security.JwtService;
import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserRole;
import com.marketplace.selleridentity.identity.repository.RoleRepository;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.repository.SellerRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SellerLifecycleIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void getMe_shouldReturnSellerProfile() throws Exception {
        User user = createActiveUser("me-seller@example.com", "+919876541001");
        Seller seller = Seller.create(user, "My Shop");
        sellerRepository.saveAndFlush(seller);
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(get("/api/v1/sellers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("My Shop"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void getMe_shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/sellers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void approveSeller_shouldTransitionToActive() throws Exception {
        User user = createActiveUser("approve@example.com", "+919876541002");
        Seller seller = Seller.create(user, "Approve Shop");
        seller = sellerRepository.saveAndFlush(seller);
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/v1/sellers/" + seller.getId() + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectSeller_shouldTransitionToRejected() throws Exception {
        User user = createActiveUser("reject@example.com", "+919876541003");
        Seller seller = Seller.create(user, "Reject Shop");
        seller = sellerRepository.saveAndFlush(seller);
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/v1/sellers/" + seller.getId() + "/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void suspendSeller_shouldTransitionFromActiveToSuspended() throws Exception {
        User user = createActiveUser("suspend@example.com", "+919876541004");
        Seller seller = Seller.create(user, "Suspend Shop");
        seller = sellerRepository.saveAndFlush(seller);
        seller.approve();
        sellerRepository.saveAndFlush(seller);
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/v1/sellers/" + seller.getId() + "/suspend")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectSeller_shouldFailForActiveSeller() throws Exception {
        User user = createActiveUser("invalid@example.com", "+919876541005");
        Seller seller = Seller.create(user, "Invalid Shop");
        seller = sellerRepository.saveAndFlush(seller);
        seller.approve(); // now ACTIVE
        sellerRepository.saveAndFlush(seller);
        String token = jwtService.generateToken(user, List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/v1/sellers/" + seller.getId() + "/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Invalid seller status transition"));
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
