package com.marketplace.selleridentity.identity.entity;

import com.marketplace.selleridentity.common.entity.BaseEntity;
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

import java.util.Objects;

/**
 * Explicit join entity representing the assignment of a {@link Role} to a {@link User}.
 *
 * <p>This replaces {@code @ManyToMany} with a first-class entity because:
 * <ul>
 *   <li>Join metadata (who assigned, when, expiry) can be added without schema redesign</li>
 *   <li>The relationship itself becomes queryable and auditable via BaseEntity timestamps</li>
 *   <li>Cascading behavior is explicit and controlled, not hidden by JPA collection magic</li>
 * </ul>
 *
 * <p>Future fields (add when security context and auth layer are implemented):
 * <ul>
 *   <li>{@code assigned_by} — UUID of the admin who granted the role</li>
 *   <li>{@code expires_at} — temporary role assignment with automatic revocation</li>
 *   <li>{@code reason} — audit trail for why the role was granted</li>
 *   <li>{@code revoked_by} — who removed the role</li>
 *   <li>{@code revoked_at} — when the role was removed</li>
 * </ul>
 */
@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_role",
                        columnNames = {"user_id", "role_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_role_user_id", columnList = "user_id"),
                @Index(name = "idx_user_role_role_id", columnList = "role_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_role")
    )
    private Role role;

    // =========================================================================
    // Factory Method
    // =========================================================================

    /**
     * Assigns a role to a user.
     *
     * @param user the user receiving the role
     * @param role the role being assigned
     * @return new UserRole assignment ready for persistence
     */
    public static UserRole assign(User user, Role role) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(role, "role must not be null");

        UserRole userRole = new UserRole();
        userRole.user = user;
        userRole.role = role;
        return userRole;
    }
}
