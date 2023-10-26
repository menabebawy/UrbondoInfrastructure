package com.urbondo.api.user.service.dto;

import jakarta.validation.constraints.NotBlank;

public record ResendConfirmationCodeRequestDto(@NotBlank String code) {
}
