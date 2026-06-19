package com.marketplace.selleridentity.identity.entity;

import com.marketplace.selleridentity.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * A one-time token used to verify a user's email or phone number.
 *
 * <p>Created during registration, consumed during verification.
 * Once used, it cannot be reused (immutable {@code used} flag).
 */
@Entity
@Table(
        name = "verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_verification_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_verification_token_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_verification_token_user")
    )
    private User user;

    @Column(name = "token", nullable = false, length = 256, updatable = false)
    private String token;

    @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    // =========================================================================
    // Factory Method
    // =========================================================================

    public static VerificationToken create(User user, String token, Instant expiresAt) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.user = user;
        verificationToken.token = token;
        verificationToken.expiresAt = expiresAt;
        verificationToken.used = false;
        return verificationToken;
    }

    // =========================================================================
    // Domain Behavior
    // =========================================================================

    /**
     * Marks this token as consumed. A token can only be used once.
     *
     * @throws IllegalStateException if the token has already been used
     */
    public void markAsUsed() {
        if (this.used) {
            throw new IllegalStateException("Verification token has already been used");
        }
        this.used = true;
    }
}
