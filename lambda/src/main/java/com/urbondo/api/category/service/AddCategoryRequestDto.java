package com.urbondo.api.category.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record AddCategoryRequestDto(@NotBlank String title) {
}
