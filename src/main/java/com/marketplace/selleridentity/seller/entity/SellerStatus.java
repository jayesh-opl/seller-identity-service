package com.marketplace.selleridentity.seller.entity;

/**
 * Lifecycle states for a seller account.
 *
 * <p>Transitions:
 * <pre>
 *   PENDING_APPROVAL → approve()   → ACTIVE
 *   PENDING_APPROVAL → reject()    → REJECTED
 *   ACTIVE           → suspend()   → SUSPENDED
 *   SUSPENDED        → reinstate() → ACTIVE
 * </pre>
 */
public enum SellerStatus {

    PENDING_APPROVAL,
    ACTIVE,
    SUSPENDED,
    REJECTED
}
