package com.marketplace.selleridentity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Seller & Identity Service.
 *
 * <p>{@code @SpringBootApplication} is placed in the root package
 * ({@code com.marketplace.selleridentity}) which enables automatic
 * component scanning of all sub-packages:
 * <ul>
 *   <li>{@code common.*} — config, audit, events, exceptions, utilities</li>
 *   <li>{@code identity.*} — user entities, repositories, services, controllers</li>
 *   <li>{@code auth.*} — authentication, security filters, token management</li>
 *   <li>{@code seller.*} — seller lifecycle, KYC, onboarding</li>
 *   <li>{@code outbox.*} — transactional outbox relay</li>
 * </ul>
 */
@SpringBootApplication
public class SellerIdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SellerIdentityServiceApplication.class, args);
    }
}
