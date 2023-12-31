package com.urbondo.api.user.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record ResendConfirmationCodeRequestDto(@NotBlank String code) {
}
