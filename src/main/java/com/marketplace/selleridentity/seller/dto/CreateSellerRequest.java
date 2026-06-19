package com.marketplace.selleridentity.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for seller onboarding.
 */
public record CreateSellerRequest(

        @NotBlank
        @Size(max = 200)
        String displayName
) {
}
