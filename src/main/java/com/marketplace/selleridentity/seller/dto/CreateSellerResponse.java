package com.marketplace.selleridentity.seller.dto;

import java.util.UUID;

/**
 * Response payload after successful seller registration.
 */
public record CreateSellerResponse(
        UUID sellerId,
        String displayName,
        String status
) {
}
