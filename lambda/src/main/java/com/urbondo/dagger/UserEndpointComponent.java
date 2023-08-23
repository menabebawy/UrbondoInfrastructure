package com.urbondo.dagger;

import com.urbondo.endpoints.UserEndpointsHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {UserServiceModule.class, ValidatorModule.class, GsonModule.class})
public interface UserEndpointComponent {
    void inject(UserEndpointsHandler userEndpointsHandler);
}
