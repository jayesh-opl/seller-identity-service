package com.marketplace.selleridentity.seller.service;

import com.marketplace.selleridentity.identity.entity.User;
import com.marketplace.selleridentity.identity.repository.UserRepository;
import com.marketplace.selleridentity.seller.dto.CreateSellerRequest;
import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.entity.Seller;
import com.marketplace.selleridentity.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles seller profile creation during onboarding.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public CreateSellerResponse createSeller(UUID authenticatedUserId, CreateSellerRequest request) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (sellerRepository.existsByUserId(authenticatedUserId)) {
            throw new IllegalArgumentException("Seller profile already exists");
        }

        Seller seller = Seller.create(user, request.displayName());
        seller = sellerRepository.save(seller);

        return new CreateSellerResponse(
                seller.getId(),
                seller.getDisplayName(),
                seller.getStatus().name()
        );
    }
}
