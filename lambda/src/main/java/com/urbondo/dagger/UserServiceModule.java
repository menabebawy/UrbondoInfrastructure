package com.urbondo.dagger;

import com.urbondo.api.user.repository.UserRepositoryImpl;
import com.urbondo.api.user.service.CognitoService;
import com.urbondo.api.user.service.CognitoServiceImpl;
import com.urbondo.api.user.service.UserService;
import com.urbondo.api.user.service.UserServiceImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import javax.inject.Singleton;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Module
public class UserServiceModule {
    @Singleton
    @Provides
    public UserService userService() {
        return new UserServiceImpl(new UserRepositoryImpl(new DynamoDBConfig().dynamoDBMapper()));
    }

    @Singleton
    @Provides
    public CognitoService cognitoService() {
        return new CognitoServiceImpl(
                CognitoIdentityProviderClient.builder()
                        .region(US_EAST_1)
                        .credentialsProvider(ProfileCredentialsProvider.create())
                        .build());
    }
}
