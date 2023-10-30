package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import com.urbondo.api.user.repository.UserDao;
import com.urbondo.api.user.service.dto.SignupRequestDto;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType.REFRESH_TOKEN_AUTH;
import static software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType.USER_PASSWORD_AUTH;

public class CognitoServiceImpl implements CognitoService {
    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final String clientId;
    private final String clientSecret;

    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";
    private static final String EMAIL = "email";
    private static final String PHONE_NUMBER = "phone_number";


    @Inject
    public CognitoServiceImpl(CognitoIdentityProviderClient cognitoIdentityProviderClient,
                              String clientId,
                              String clientSecret) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public JsonObject signup(SignupRequestDto signupRequestDto) {
        AttributeType emailAttribute = AttributeType.builder().name(EMAIL).value(signupRequestDto.email()).build();
        AttributeType firstNameAttribute = AttributeType.builder()
                .name(GIVEN_NAME)
                .value(signupRequestDto.firstName())
                .build();

        AttributeType lastNameAttribute = AttributeType.builder()
                .name(FAMILY_NAME)
                .value(signupRequestDto.lastName())
                .build();

        AttributeType phoneAttribute = AttributeType.builder()
                .name(PHONE_NUMBER)
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
                .secretHash(calculateSecretHash(signupRequestDto.email()))
                .build();

        SignUpResponse signUpResponse = cognitoIdentityProviderClient.signUp(signUpRequest);

        JsonObject createUserResult = new JsonObject();
        createUserResult.addProperty("isSuccessful", signUpResponse.sdkHttpResponse().isSuccessful());
        createUserResult.addProperty("statusCode", signUpResponse.sdkHttpResponse().statusCode());
        createUserResult.addProperty("cognitoUserId", signUpResponse.userSub());
        createUserResult.addProperty("isConfirmed", signUpResponse.userConfirmed());

        return createUserResult;
    }

    public String calculateSecretHash(String userName) {
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
    public JsonObject confirmSignUp(String code, String username) {
        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(username)
                .confirmationCode(code)
                .secretHash(calculateSecretHash(username))
                .build();
        cognitoIdentityProviderClient.confirmSignUp(confirmSignUpRequest);
        JsonObject responsJsonObject = new JsonObject();
        responsJsonObject.addProperty("message", username + " is confirmed");
        return responsJsonObject;
    }

    @Override
    public UserDao getUser(String accessToken) {
        GetUserResponse cognitoUser = cognitoIdentityProviderClient.getUser(GetUserRequest.builder()
                                                                                    .accessToken(accessToken)
                                                                                    .build());
        return new UserDao(cognitoUser.username(),
                           getValueOfAttribute(cognitoUser, GIVEN_NAME),
                           getValueOfAttribute(cognitoUser, FAMILY_NAME),
                           getValueOfAttribute(cognitoUser, EMAIL),
                           getValueOfAttribute(cognitoUser, PHONE_NUMBER));
    }

    private static String getValueOfAttribute(GetUserResponse cognitoUser, String name) {
        Optional<AttributeType> attribute = cognitoUser.userAttributes()
                .stream()
                .filter(attributeType -> attributeType.name().equals(name))
                .findFirst();

        if (attribute.isPresent())
            return attribute.get().value();

        return "";
    }

    @Override
    public JsonObject resendConfirmationCode(String userName) {
        ResendConfirmationCodeRequest codeRequest = ResendConfirmationCodeRequest.builder()
                .clientId(clientId)
                .username(userName)
                .build();

        ResendConfirmationCodeResponse response = cognitoIdentityProviderClient.resendConfirmationCode(codeRequest);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("method", response.codeDeliveryDetails().deliveryMediumAsString());
        return jsonResponse;
    }

    @Override
    public AuthenticationResultType login(String username, String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);
        authParameters.put("SECRET_HASH", calculateSecretHash(username));

        return initiateAuth(authParameters, USER_PASSWORD_AUTH);
    }

    @Override
    public AuthenticationResultType refreshToken(String refreshToken, String username) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", refreshToken);
        authParameters.put("SECRET_HASH", calculateSecretHash(username));

        return initiateAuth(authParameters, REFRESH_TOKEN_AUTH);
    }

    private AuthenticationResultType initiateAuth(Map<String, String> params, AuthFlowType refreshTokenAuth) {
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authParameters(params)
                .authFlow(refreshTokenAuth)
                .build();

        return cognitoIdentityProviderClient.initiateAuth(authRequest).authenticationResult();
    }
}