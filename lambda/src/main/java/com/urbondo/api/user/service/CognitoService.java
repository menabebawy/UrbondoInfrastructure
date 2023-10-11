package com.urbondo.api.user.service;

import com.google.gson.JsonObject;

public interface CognitoService {
    JsonObject signup(SignupRequestDto signupRequestDto, String userPoolId);
}
