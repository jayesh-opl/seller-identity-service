package com.marketplace.selleridentity.auth.dto;

/**
 * Response payload containing the issued access token.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
