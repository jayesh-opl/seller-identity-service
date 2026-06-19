package com.marketplace.selleridentity.seller.service;

import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Manages seller lifecycle transitions and profile retrieval.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SellerLifecycleService {

    private final SellerRepository sellerRepository;

    @Transactional(readOnly = true)
    public CreateSellerResponse getCurrentSeller(UUID authenticatedUserId) {
        Seller seller = sellerRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));

        return new CreateSellerResponse(
                seller.getId(),
                seller.getDisplayName(),
                seller.getStatus().name()
        );
    }

    public void approveSeller(UUID sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.approve();
    }

    public void rejectSeller(UUID sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.reject();
    }

    public void suspendSeller(UUID sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.suspend();
    }

    private Seller findSellerById(UUID sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
    }
}
