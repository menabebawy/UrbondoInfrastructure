package com.urbondo;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

public class CognitoStack extends Stack {
    private static final Logger logger = LoggerFactory.getLogger(CognitoStack.class);
    private static final String URBONDO_USER_POOL_NAME = "urbondo-user-pool";

    public CognitoStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
        CognitoIdentityProviderClient cognitoClient = cognitoProviderClient();
        if (userPoolExists(cognitoClient))
            return;
        String userPoolId = createUserPool(cognitoClient).userPool().id();
        createUserPoolClient(cognitoClient, userPoolId);
    }

    private static CognitoIdentityProviderClient cognitoProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    private static CreateUserPoolResponse createUserPool(CognitoIdentityProviderClient cognitoClient) {
        // Allow user to sign in using their email
        List<String> aliasAttributes = new ArrayList<>();
        aliasAttributes.add("email");

        try {
            CreateUserPoolRequest poolRequest = CreateUserPoolRequest.builder()
                    .poolName(URBONDO_USER_POOL_NAME)
                    .aliasAttributesWithStrings(aliasAttributes)
                    .policies(userPoolPolicyType -> userPoolPolicyType.passwordPolicy(
                            // Don't force password to include symbols
                            passwordPolicyType -> passwordPolicyType.minimumLength(6).requireSymbols(false)))
                    .build();
            return cognitoClient.createUserPool(poolRequest);
        } catch (CognitoIdentityProviderException exception) {
            logger.error(exception.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return null;
    }

    private static void createUserPoolClient(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
        try {
            CreateUserPoolClientRequest poolClientRequest = CreateUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientName("urbondo-user-pool-client")
                    .generateSecret(true)
                    .build();
            cognitoClient.createUserPoolClient(poolClientRequest);
        } catch (CognitoIdentityProviderException exception) {
            logger.error(exception.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    private static boolean userPoolExists(CognitoIdentityProviderClient cognitoClient) {
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder()
                .maxResults(10)
                .build();
        return cognitoClient.listUserPools(listUserPoolsRequest).userPools()
                .stream()
                .anyMatch(userPoolDescriptionType -> userPoolDescriptionType.name().equals(URBONDO_USER_POOL_NAME));
    }
}
