package com.urbondo.api.user.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmationCodeRequestDto(@NotBlank @Email String email, @NotBlank String code) {
}
