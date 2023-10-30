package com.urbondo.api.user.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record RefreshTokenRequestDto(@NotBlank String username, @NotBlank String refreshToken) {
}
