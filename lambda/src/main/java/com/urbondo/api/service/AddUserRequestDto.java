package com.urbondo.api.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Valid
public record AddUserRequestDto(@NotBlank String firstName,
                                @NotBlank String lastName,
                                @NotBlank @Email String email,
                                @NotBlank String phone) {
}
