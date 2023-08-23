package com.urbondo.api.service;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDto(@NotBlank String id,
                                   @NotBlank String firstName,
                                   @NotBlank String lastName,
                                   @NotBlank String phone) {
}
