package com.marketplace.selleridentity.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA Auditing for automatic population of
 * {@code @CreatedDate} and {@code @LastModifiedDate} fields in entities
 * annotated with {@code @EntityListeners(AuditingEntityListener.class)}.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
