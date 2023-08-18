package com.urbondo.dagger;

import com.google.gson.Gson;
import com.urbondo.api.service.UserService;
import com.urbondo.endpoints.UserEndpointsHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = UserServiceModule.class)
public interface UserEndpointComponent {
    void inject(UserEndpointsHandler userEndpointsHandler);

    UserService provideUserService();

    Gson provideGson();
}
