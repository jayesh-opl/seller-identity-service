package com.marketplace.selleridentity.seller.controller;

import com.marketplace.selleridentity.seller.dto.CreateSellerRequest;
import com.marketplace.selleridentity.seller.dto.CreateSellerResponse;
import com.marketplace.selleridentity.seller.service.SellerLifecycleService;
import com.marketplace.selleridentity.seller.service.SellerOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller")
public class SellerController {

    private final SellerOnboardingService sellerOnboardingService;
    private final SellerLifecycleService sellerLifecycleService;

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

    @GetMapping("/me")
    @Operation(
            summary = "Get current seller profile",
            description = "Returns the seller profile for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seller profile found"),
                    @ApiResponse(responseCode = "400", description = "No seller profile exists"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public ResponseEntity<CreateSellerResponse> getCurrentSeller(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        CreateSellerResponse response = sellerLifecycleService.getCurrentSeller(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sellerId}/approve")
    @Operation(
            summary = "Approve seller",
            description = "Transitions seller from PENDING_APPROVAL to ACTIVE.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Seller approved"),
                    @ApiResponse(responseCode = "400", description = "Invalid state transition"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public ResponseEntity<Void> approveSeller(@PathVariable UUID sellerId) {
        sellerLifecycleService.approveSeller(sellerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sellerId}/reject")
    @Operation(
            summary = "Reject seller",
            description = "Transitions seller from PENDING_APPROVAL to REJECTED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Seller rejected"),
                    @ApiResponse(responseCode = "400", description = "Invalid state transition"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public ResponseEntity<Void> rejectSeller(@PathVariable UUID sellerId) {
        sellerLifecycleService.rejectSeller(sellerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sellerId}/suspend")
    @Operation(
            summary = "Suspend seller",
            description = "Transitions seller from ACTIVE to SUSPENDED.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Seller suspended"),
                    @ApiResponse(responseCode = "400", description = "Invalid state transition"),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public ResponseEntity<Void> suspendSeller(@PathVariable UUID sellerId) {
        sellerLifecycleService.suspendSeller(sellerId);
        return ResponseEntity.noContent().build();
    }
}
