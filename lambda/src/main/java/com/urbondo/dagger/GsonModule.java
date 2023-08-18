package com.urbondo.dagger;

import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class GsonModule {
    @Singleton
    @Provides
    public Gson gson() {
        return new Gson();
    }
}
