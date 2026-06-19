package com.marketplace.selleridentity.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity providing identity, versioning, and timestamp fields for all JPA entities.
 *
 * <p>Every table in this service inherits these columns:
 * <ul>
 *   <li>{@code id} — UUID primary key (application-generated)</li>
 *   <li>{@code created_at} — immutable creation timestamp in UTC</li>
 *   <li>{@code updated_at} — last modification timestamp in UTC</li>
 *   <li>{@code version} — optimistic lock counter</li>
 * </ul>
 *
 * <p>Uses {@link MappedSuperclass} so fields are mapped to the child entity's table
 * (no separate table or join inheritance). This keeps the database schema clean.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
