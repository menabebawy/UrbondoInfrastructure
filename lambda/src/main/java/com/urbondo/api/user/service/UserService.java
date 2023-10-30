package com.urbondo.api.user.service;


import com.google.gson.JsonObject;
import com.urbondo.api.user.repository.UserDao;
import com.urbondo.api.user.service.dto.SignupRequestDto;
import com.urbondo.api.user.service.dto.UpdateUserRequestDto;
import com.urbondo.lib.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public interface UserService {
    JsonObject signup(SignupRequestDto signupRequestDto);

    JsonObject confirmSignUp(String code, String username);

    JsonObject resendConfirmationCode(String userName);

    AuthenticationResultType login(String username, String password);

    AuthenticationResultType refreshToken(String refreshToken, String username);

    UserDao findById(String id) throws ResourceNotFoundException;

    UserDao update(UpdateUserRequestDto updateUserRequestDTO);

    void deleteBy(String id);
}