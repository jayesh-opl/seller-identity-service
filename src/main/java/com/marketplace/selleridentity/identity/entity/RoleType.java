package com.marketplace.selleridentity.identity.entity;

/**
 * Enumeration of platform user roles.
 *
 * <p>Stored as VARCHAR in the database via {@code @Enumerated(EnumType.STRING)}.
 * Adding a new role requires:
 * <ol>
 *   <li>Add the enum constant here</li>
 *   <li>Insert a row in the {@code role} table via a Flyway migration</li>
 * </ol>
 */
public enum RoleType {

    CUSTOMER,
    SELLER,
    ADMIN,
    SUPPORT_AGENT
}
