package com.urbondo.dagger;

import com.urbondo.api.user.repository.UserRepositoryImpl;
import com.urbondo.api.user.service.UserService;
import com.urbondo.api.user.service.UserServiceImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class UserServiceModule {
    @Singleton
    @Provides
    public UserService userService() {
        return new UserServiceImpl(new UserRepositoryImpl(new DynamoDBConfig().dynamoDBMapper()));
    }
}
