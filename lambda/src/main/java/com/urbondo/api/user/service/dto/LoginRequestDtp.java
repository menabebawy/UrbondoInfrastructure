package com.urbondo.api.user.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Valid
public record LoginRequestDtp(@NotBlank @Email String username, @NotBlank String password) {
}
