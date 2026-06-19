package com.marketplace.selleridentity.identity.service;

import com.marketplace.selleridentity.identity.entity.VerificationToken;
import com.marketplace.selleridentity.identity.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Handles email/phone verification by consuming one-time tokens.
 *
 * <p>Relies on JPA dirty checking — modified entities are flushed
 * automatically at transaction commit without explicit save calls.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;

    public void verify(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Verification token already used");
        }

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token expired");
        }

        verificationToken.markAsUsed();
        verificationToken.getUser().activate();
    }
}
