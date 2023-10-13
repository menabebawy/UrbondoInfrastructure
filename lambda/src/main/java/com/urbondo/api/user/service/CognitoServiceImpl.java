package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CognitoServiceImpl implements CognitoService {
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Inject
    public CognitoServiceImpl(CognitoIdentityProviderClient cognitoIdentityProviderClient) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    @Override
    public JsonObject signup(SignupRequestDto signupRequestDto, String clientId, String clientSecret) {
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
                .secretHash(calculateSecretHash(clientId, clientSecret, signupRequestDto.email()))
                .build();

        SignUpResponse signUpResponse = cognitoIdentityProviderClient.signUp(signUpRequest);

        JsonObject createUserResult = new JsonObject();
        createUserResult.addProperty("isSuccessful", signUpResponse.sdkHttpResponse().isSuccessful());
        createUserResult.addProperty("statusCode", signUpResponse.sdkHttpResponse().statusCode());
        createUserResult.addProperty("cognitoUserId", signUpResponse.userSub());
        createUserResult.addProperty("isConfirmed", signUpResponse.userConfirmed());

        return createUserResult;
    }

    public static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception exception) {
            throw new CalculatingSecretHashException(exception.getMessage());
        }
    }
}
