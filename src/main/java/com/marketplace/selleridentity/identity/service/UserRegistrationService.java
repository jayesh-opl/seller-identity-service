package com.marketplace.selleridentity.identity.service;

import com.marketplace.selleridentity.identity.dto.RegisterUserRequest;
import com.marketplace.selleridentity.identity.dto.RegisterUserResponse;
import com.marketplace.selleridentity.identity.entity.Role;
import com.marketplace.selleridentity.identity.entity.RoleType;
import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.entity.UserRole;
import com.marketplace.selleridentity.identity.entity.VerificationToken;
import com.marketplace.selleridentity.identity.repository.RoleRepository;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.identity.repository.UserRoleRepository;
import com.marketplace.selleridentity.identity.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Handles new user registration with secure password hashing.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserResponse register(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered: " + request.phoneNumber());
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        );

        user = userRepository.save(user);

        Role customerRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "CUSTOMER role not found. Ensure V1 migration has been applied."));

        UserRole userRole = UserRole.assign(user, customerRole);
        userRoleRepository.save(userRole);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.create(
                user,
                token,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        verificationTokenRepository.save(verificationToken);

        log.info("Verification token for user {}: {}", user.getEmail(), token);

        return new RegisterUserResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus().name()
        );
    }
}
