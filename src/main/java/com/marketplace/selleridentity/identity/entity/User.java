package com.marketplace.selleridentity.identity.entity;

import com.marketplace.selleridentity.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Core identity entity representing any platform user (customer, seller, admin, support agent).
 *
 * <p>This entity is the authentication record — it owns credentials and account state.
 * Profile details (avatar, DOB) and role assignments live in separate entities
 * to keep this aggregate focused on identity and security concerns.
 *
 * <p>Follows DDD principles:
 * <ul>
 *   <li>No public setters — state changes through domain methods with guard clauses</li>
 *   <li>Factory method for controlled creation</li>
 *   <li>Invariants enforced within the entity itself</li>
 * </ul>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_user_phone_number", columnNames = "phone_number")
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_phone_number", columnList = "phone_number"),
                @Index(name = "idx_user_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    // =========================================================================
    // Factory Method
    // =========================================================================

    /**
     * Creates a new user in PENDING_VERIFICATION state.
     * The caller is responsible for hashing the password before passing it here.
     *
     * @param email        unique email address
     * @param passwordHash pre-hashed password (bcrypt/argon2)
     * @param firstName    user's first name
     * @param lastName     user's last name
     * @param phoneNumber  unique phone number (E.164 format recommended)
     * @return new User instance ready for persistence
     */
    public static User create(String email,
                              String passwordHash,
                              String firstName,
                              String lastName,
                              String phoneNumber) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");

        User user = new User();
        user.email = email.toLowerCase().trim();
        user.passwordHash = passwordHash;
        user.firstName = firstName.trim();
        user.lastName = lastName.trim();
        user.phoneNumber = phoneNumber.trim();
        user.status = UserStatus.PENDING_VERIFICATION;
        return user;
    }

    // =========================================================================
    // Domain Behavior — State Transitions
    // =========================================================================

    /**
     * Activates the user account after successful email/phone verification.
     * This is a one-time transition from the initial registration state.
     *
     * @throws IllegalStateException if the account is not in PENDING_VERIFICATION state
     */
    public void activate() {
        if (this.status != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException(
                    "Cannot activate user in status: " + this.status);
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Unlocks a previously locked account (admin action or cooldown expiry).
     *
     * @throws IllegalStateException if the account is not in LOCKED state
     */
    public void unlock() {
        if (this.status != UserStatus.LOCKED) {
            throw new IllegalStateException(
                    "Cannot unlock user in status: " + this.status);
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Locks the account due to security concerns (e.g., brute-force detection, admin action).
     *
     * @throws IllegalStateException if the account is not currently ACTIVE
     */
    public void lock() {
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot lock user in status: " + this.status);
        }
        this.status = UserStatus.LOCKED;
    }

    /**
     * Disables the account permanently (self-deactivation or admin action).
     *
     * @throws IllegalStateException if the account is already DISABLED
     */
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            throw new IllegalStateException("User is already disabled");
        }
        this.status = UserStatus.DISABLED;
    }

    // =========================================================================
    // Domain Behavior — Credential Management
    // =========================================================================

    /**
     * Changes the user's password hash. The caller must handle:
     * <ul>
     *   <li>Verifying the old password before calling this</li>
     *   <li>Hashing the new password (never store plaintext)</li>
     * </ul>
     *
     * @param newPasswordHash the new pre-hashed password
     * @throws IllegalArgumentException if the hash is null or empty
     */
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        this.passwordHash = newPasswordHash;
    }

    // =========================================================================
    // Domain Behavior — Profile Updates
    // =========================================================================

    /**
     * Updates the user's display name.
     *
     * @param firstName new first name
     * @param lastName  new last name
     */
    public void updateName(String firstName, String lastName) {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }
}
