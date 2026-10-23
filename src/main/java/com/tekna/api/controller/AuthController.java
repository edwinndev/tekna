package com.tekna.api.controller;

import com.tekna.api.dto.AuthOutput;
import com.tekna.api.dto.LoginInput;
import com.tekna.api.dto.RefreshTokenInput;
import com.tekna.api.dto.RegisterInput;
import com.tekna.api.dto.UserOutput;
import com.tekna.api.security.CustomUserDetails;
import com.tekna.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthOutput> login(@Valid @RequestBody LoginInput input) {
        return ResponseEntity.ok(authService.login(input));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
    })
    public ResponseEntity<AuthOutput> register(@Valid @RequestBody RegisterInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(input));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthOutput> refreshToken(@Valid @RequestBody RefreshTokenInput input) {
        return ResponseEntity.ok(authService.refreshToken(input));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token and logout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logged out successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenInput input) {
        authService.logout(input.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    public ResponseEntity<UserOutput> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(authService.getMe(userDetails.getId()));
    }
}
