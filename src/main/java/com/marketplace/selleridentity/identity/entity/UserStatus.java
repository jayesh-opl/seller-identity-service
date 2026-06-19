package com.marketplace.selleridentity.identity.entity;

/**
 * Lifecycle states for a platform user account.
 *
 * <p>State transitions:
 * <pre>
 *   PENDING_VERIFICATION → ACTIVE (email/phone verified)
 *   ACTIVE → LOCKED (too many failed login attempts or admin action)
 *   ACTIVE → DISABLED (user self-deactivation or admin action)
 *   LOCKED → ACTIVE (admin unlocks or cooldown expires)
 *   DISABLED → ACTIVE (admin reactivation)
 * </pre>
 *
 * <p>Stored as VARCHAR in the database via {@code @Enumerated(EnumType.STRING)}.
 */
public enum UserStatus {

    /** Account created but email/phone not yet verified. Cannot transact. */
    PENDING_VERIFICATION,

    /** Fully verified and operational account. */
    ACTIVE,

    /** Temporarily locked due to security concern. Cannot login. */
    LOCKED,

    /** Permanently or voluntarily disabled. Cannot login or transact. */
    DISABLED
}
