package com.urbondo.dagger;

import com.urbondo.endpoints.CategoryEndpointsHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CategoryServiceModule.class, ValidatorModule.class, GsonModule.class})
public interface CategoryEndpointComponent {
    void inject(CategoryEndpointsHandler categoryEndpointsHandler);
}
