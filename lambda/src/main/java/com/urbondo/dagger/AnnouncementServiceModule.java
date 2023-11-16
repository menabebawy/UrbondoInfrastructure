package com.urbondo.dagger;

import com.urbondo.api.announcement.repositoy.AnnouncementRepositoryImpl;
import com.urbondo.api.announcement.service.AnnouncementService;
import com.urbondo.api.announcement.service.AnnouncementServiceImpl;
import com.urbondo.api.category.repository.CategoryRepositoryImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AnnouncementServiceModule {

    @Singleton
    @Provides
    public AnnouncementService announcementService() {
        return new AnnouncementServiceImpl(
                new AnnouncementRepositoryImpl(DynamoDBConfig.getInstance().getDynamoDBMapper()),
                new CategoryRepositoryImpl(DynamoDBConfig.getInstance().getDynamoDBMapper()));
    }
}
