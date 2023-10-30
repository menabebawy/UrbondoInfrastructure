package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import com.urbondo.api.user.repository.UserDao;
import com.urbondo.api.user.service.dto.SignupRequestDto;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public interface CognitoService {
    JsonObject signup(SignupRequestDto signupRequestDto);

    JsonObject confirmSignUp(String code, String username);

    UserDao getUser(String accessToken);

    JsonObject resendConfirmationCode(String email);

    AuthenticationResultType login(String username, String password);

    AuthenticationResultType refreshToken(String refreshToken, String username);
}
