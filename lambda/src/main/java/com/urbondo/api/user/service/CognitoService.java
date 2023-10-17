package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public interface CognitoService {
    JsonObject signup(SignupRequestDto signupRequestDto, String clientId, String clientSecret);

    JsonObject confirmSignUp(String clientId, String code, String username);

    AuthenticationResultType initiateAuth(String clientId, String userPoolId, String username, String password);
}
