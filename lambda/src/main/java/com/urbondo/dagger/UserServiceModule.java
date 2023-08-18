package com.urbondo.dagger;

import com.google.gson.Gson;
import com.urbondo.api.repository.UserRepositoryImpl;
import com.urbondo.api.service.UserService;
import com.urbondo.api.service.UserServiceImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import javax.inject.Singleton;

@Module
public class UserServiceModule {

    @Singleton
    @Provides
    public UserService userService() {
        return new UserServiceImpl(new UserRepositoryImpl(new DynamoDBConfig().dynamoDBMapper()));
    }

    @Singleton
    @Provides
    public Gson gson() {
        return new Gson();
    }

    @Singleton
    @Provides
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
