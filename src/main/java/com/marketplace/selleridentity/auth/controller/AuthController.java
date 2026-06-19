package com.marketplace.selleridentity.auth.controller;

import com.marketplace.selleridentity.auth.dto.LoginRequest;
import com.marketplace.selleridentity.auth.dto.LoginResponse;
import com.marketplace.selleridentity.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates an active user and returns a JWT access token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials or validation error"),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error")
            }
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info from JWT")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "subject", authentication.getName()
        ));
    }
}
