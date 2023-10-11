package com.urbondo.api.user.service;

public record SignupRequestDto(String email, String passowrd, String firstName, String lastName, String phone) {
}
