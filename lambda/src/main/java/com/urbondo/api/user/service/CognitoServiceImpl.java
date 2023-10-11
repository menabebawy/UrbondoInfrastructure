package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;

import java.util.ArrayList;
import java.util.List;

public class CognitoServiceImpl implements CognitoService {
    @Override
    public JsonObject signup(SignupRequestDto signupRequestDto, String clientId) {
        AttributeType emailAttribute = AttributeType.builder()
                .name("email")
                .value(signupRequestDto.email())
                .build();

        AttributeType firstNameAttribute = AttributeType.builder()
                .name("firstName")
                .value(signupRequestDto.firstName())
                .build();

        AttributeType lastNameAttribute = AttributeType.builder()
                .name("lastName")
                .value(signupRequestDto.lastName())
                .build();

        AttributeType phoneAttribute = AttributeType.builder()
                .name("phone")
                .value(signupRequestDto.phone())
                .build();

        List<AttributeType> attributeTypeList = new ArrayList<>();
        attributeTypeList.add(emailAttribute);
        attributeTypeList.add(firstNameAttribute);
        attributeTypeList.add(lastNameAttribute);
        attributeTypeList.add(phoneAttribute);

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(signupRequestDto.email())
                .password(signupRequestDto.passowrd())
                .clientId(clientId)
                .userAttributes(attributeTypeList)
                .secretHash("")
                .build();

        return null;
    }
}
