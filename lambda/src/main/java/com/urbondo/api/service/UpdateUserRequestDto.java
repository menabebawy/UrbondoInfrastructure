package com.urbondo.api.service;

public record UpdateUserRequestDto(String id,
                                   String firstName,
                                   String lastName,
                                   String phone) {
}
