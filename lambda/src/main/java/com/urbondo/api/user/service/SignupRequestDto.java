package com.urbondo.api.user.service;

public record SignupRequestDto(String email, String password, String firstName, String lastName, String phone) {
}
