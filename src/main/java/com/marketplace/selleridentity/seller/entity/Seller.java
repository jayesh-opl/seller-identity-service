package com.marketplace.selleridentity.seller.entity;

import com.marketplace.selleridentity.common.entity.BaseEntity;
import com.marketplace.selleridentity.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Represents a seller profile linked to an authenticated user.
 * A user can have at most one seller profile (enforced by unique constraint).
 */
@Entity
@Table(
        name = "sellers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_seller_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_seller_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_seller_user")
    )
    private User user;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SellerStatus status;

    // =========================================================================
    // Factory Method
    // =========================================================================

    public static Seller create(User user, String displayName) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");

        Seller seller = new Seller();
        seller.user = user;
        seller.displayName = displayName.trim();
        seller.status = SellerStatus.PENDING_APPROVAL;
        return seller;
    }

    // =========================================================================
    // Domain Behavior — Lifecycle Transitions
    // =========================================================================

    /**
     * Approves the seller application.
     * Only valid from PENDING_APPROVAL.
     */
    public void approve() {
        if (this.status != SellerStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Invalid seller status transition");
        }
        this.status = SellerStatus.ACTIVE;
    }

    /**
     * Rejects the seller application.
     * Only valid from PENDING_APPROVAL. Terminal state.
     */
    public void reject() {
        if (this.status != SellerStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Invalid seller status transition");
        }
        this.status = SellerStatus.REJECTED;
    }

    /**
     * Suspends an active seller due to policy violation or admin action.
     * Only valid from ACTIVE.
     */
    public void suspend() {
        if (this.status != SellerStatus.ACTIVE) {
            throw new IllegalStateException("Invalid seller status transition");
        }
        this.status = SellerStatus.SUSPENDED;
    }
}
