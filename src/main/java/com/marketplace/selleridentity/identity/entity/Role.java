package com.marketplace.selleridentity.identity.entity;

import com.marketplace.selleridentity.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a platform role that can be assigned to users.
 *
 * <p>Each role is a row in the {@code roles} table, seeded via Flyway migration.
 * Using a dedicated table (rather than a column on the user table) allows:
 * <ul>
 *   <li>Users to hold multiple roles in the future (many-to-many)</li>
 *   <li>Role metadata (description, permissions) to be attached without schema changes</li>
 *   <li>Referential integrity — only defined roles can be assigned</li>
 * </ul>
 *
 * <p>This entity follows DDD principles: state changes go through explicit
 * domain methods, not unrestricted setters.
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_role_name", columnNames = "name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    /**
     * The role identifier stored as a string (e.g., "CUSTOMER", "SELLER").
     * VARCHAR avoids PostgreSQL ENUM type migration headaches when adding new roles.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 50)
    private RoleType name;

    /**
     * Human-readable description for admin UIs and audit logs.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Factory method for creating a role. Preferred over public constructor
     * to maintain control over entity instantiation.
     */
    public static Role of(RoleType roleType, String description) {
        Role role = new Role();
        role.name = roleType;
        role.description = description;
        return role;
    }

    /**
     * Updates the role description. This is the only mutable field —
     * role name is immutable after creation (changing a role's identity
     * requires creating a new role).
     *
     * @param description new human-readable description
     */
    public void updateDescription(String description) {
        this.description = description;
    }
}
