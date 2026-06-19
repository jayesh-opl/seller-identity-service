package com.marketplace.selleridentity.auth;

import com.marketplace.selleridentity.auth.dto.LoginRequest;
import com.marketplace.selleridentity.auth.dto.LoginResponse;
import com.marketplace.selleridentity.auth.security.JwtProperties;
import com.marketplace.selleridentity.auth.security.JwtService;
import com.marketplace.selleridentity.auth.service.AuthenticationService;
import com.marketplace.selleridentity.identity.entity.Role;
import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserRole;
import com.marketplace.selleridentity.identity.entity.UserStatus;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User activeUser;
    private User pendingUser;

    @BeforeEach
    void setUp() throws Exception {
        activeUser = createUser("active@example.com", "+919876543210", UserStatus.ACTIVE);
        pendingUser = createUser("pending@example.com", "+919876543211", UserStatus.PENDING_VERIFICATION);
    }

    @Test
    void login_shouldReturnTokenOnSuccess() {
        LoginRequest request = new LoginRequest("active@example.com", "password123");

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", activeUser.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(activeUser.getId())).thenReturn(List.of());
        when(jwtService.generateToken(eq(activeUser), any())).thenReturn("mocked.jwt.token");
        when(jwtProperties.expirationSeconds()).thenReturn(3600L);

        LoginResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void login_shouldThrowWhenEmailNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldThrowWhenPasswordIncorrect() {
        LoginRequest request = new LoginRequest("active@example.com", "wrongpassword");

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpassword", activeUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldThrowWhenUserNotActive() {
        LoginRequest request = new LoginRequest("pending@example.com", "password123");

        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.matches("password123", pendingUser.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User account is not active");
    }

    @Test
    void login_shouldIncludeRolesInToken() {
        LoginRequest request = new LoginRequest("active@example.com", "password123");

        Role customerRole = Role.of(RoleType.CUSTOMER, "Customer");
        setId(customerRole, UUID.randomUUID());
        UserRole userRole = UserRole.assign(activeUser, customerRole);

        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", activeUser.getPasswordHash())).thenReturn(true);
        when(userRoleRepository.findByUserId(activeUser.getId())).thenReturn(List.of(userRole));
        when(jwtService.generateToken(eq(activeUser), eq(List.of("CUSTOMER")))).thenReturn("token.with.roles");
        when(jwtProperties.expirationSeconds()).thenReturn(3600L);

        LoginResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo("token.with.roles");
    }

    private User createUser(String email, String phone, UserStatus status) throws Exception {
        User user = User.create(email, "$2a$10$hashedpassword", "Test", "User", phone);
        setId(user, UUID.randomUUID());
        if (status == UserStatus.ACTIVE) {
            user.activate();
        }
        return user;
    }

    private void setId(Object entity, UUID id) {
        try {
            Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
