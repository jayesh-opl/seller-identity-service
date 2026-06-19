package com.marketplace.selleridentity.seller.controller;

import com.marketplace.selleridentity.seller.dto.CreateSellerRequest;
import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.service.SellerOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller Onboarding")
public class SellerController {

    private final SellerOnboardingService sellerOnboardingService;

    @PostMapping
    @Operation(
            summary = "Create seller profile",
            description = "Creates a seller profile for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Seller profile created"),
                    @ApiResponse(responseCode = "400", description = "Seller already exists or validation error"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public ResponseEntity<CreateSellerResponse> createSeller(
            Authentication authentication,
            @Valid @RequestBody CreateSellerRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        CreateSellerResponse response = sellerOnboardingService.createSeller(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
