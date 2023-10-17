package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType.USER_PASSWORD_AUTH;

public class CognitoServiceImpl implements CognitoService {
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Inject
    public CognitoServiceImpl(CognitoIdentityProviderClient cognitoIdentityProviderClient) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    @Override
    public JsonObject signup(SignupRequestDto signupRequestDto, String clientId, String clientSecret) {
        AttributeType emailAttribute = AttributeType.builder().name("email").value(signupRequestDto.email()).build();
        AttributeType firstNameAttribute = AttributeType.builder()
                .name("given_name")
                .value(signupRequestDto.firstName())
                .build();

        AttributeType lastNameAttribute = AttributeType.builder()
                .name("family_name")
                .value(signupRequestDto.lastName())
                .build();

        AttributeType phoneAttribute = AttributeType.builder()
                .name("phone_number")
                .value(signupRequestDto.phone())
                .build();

        List<AttributeType> attributeTypeList = new ArrayList<>();
        attributeTypeList.add(emailAttribute);
        attributeTypeList.add(firstNameAttribute);
        attributeTypeList.add(lastNameAttribute);
        attributeTypeList.add(phoneAttribute);

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(signupRequestDto.email())
                .password(signupRequestDto.password())
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

    public static String calculateSecretHash(String clientId, String clientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(UTF_8), HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception exception) {
            throw new CalculatingSecretHashException(exception.getMessage());
        }
    }

    @Override
    public JsonObject confirmSignUp(String clientId, String code, String username) {
        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(username)
                .confirmationCode(code)
                .build();
        cognitoIdentityProviderClient.confirmSignUp(confirmSignUpRequest);
        JsonObject responsJsonObject = new JsonObject();
        responsJsonObject.addProperty("message", username + " is confirmed");
        return responsJsonObject;
    }

    @Override
    public AuthenticationResultType initiateAuth(String clientId,
                                                 String clientSecret,
                                                 String username,
                                                 String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);
        authParameters.put("SECRET_HASH", calculateSecretHash(clientId, clientSecret, username));

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authParameters(authParameters)
                .authFlow(USER_PASSWORD_AUTH)
                .build();

        return cognitoIdentityProviderClient.initiateAuth(authRequest).authenticationResult();
    }
}