package com.marketplace.selleridentity.identity.dto;

import java.util.UUID;

public record RegisterUserResponse(
        UUID userId,
        String email,
        String status
) {
}
