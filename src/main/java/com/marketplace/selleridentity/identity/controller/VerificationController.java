package com.marketplace.selleridentity.identity.controller;

import com.marketplace.selleridentity.identity.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@Tag(name = "Verification")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/{token}")
    @Operation(summary = "Verify user account")
    public ResponseEntity<Void> verify(@PathVariable String token) {
        verificationService.verify(token);
        return ResponseEntity.noContent().build();
    }
}
