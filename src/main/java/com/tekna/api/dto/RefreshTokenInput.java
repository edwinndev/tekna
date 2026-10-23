package com.tekna.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenInput {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
