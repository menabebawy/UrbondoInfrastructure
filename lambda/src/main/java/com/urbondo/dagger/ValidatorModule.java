package com.urbondo.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import javax.inject.Singleton;

@Module
public class ValidatorModule {
    @Singleton
    @Provides
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
