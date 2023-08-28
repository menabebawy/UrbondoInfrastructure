package com.urbondo.dagger;

import com.urbondo.endpoints.AnnouncementEndpointsHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AnnouncementServiceModule.class, ValidatorModule.class, GsonModule.class})
public interface AnnouncementEndpointComponent {
    void inject(AnnouncementEndpointsHandler announcementEndpointsHandler);
}
