package com.urbondo.api.user.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Valid
public record SignupRequestDto(@NotBlank @Email String email,
                               @NotBlank String password,
                               @NotBlank String firstName,
                               @NotBlank String lastName,
                               @NotBlank String phone) {
}
