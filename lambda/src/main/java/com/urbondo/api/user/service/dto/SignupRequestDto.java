package com.urbondo.api.user.service.dto;

public record SignupRequestDto(String email, String password, String firstName, String lastName, String phone) {
}
