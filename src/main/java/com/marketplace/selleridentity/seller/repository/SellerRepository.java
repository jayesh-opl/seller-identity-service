package com.marketplace.selleridentity.seller.repository;

import com.marketplace.selleridentity.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {

    Optional<Seller> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
