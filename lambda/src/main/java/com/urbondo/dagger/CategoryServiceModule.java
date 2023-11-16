package com.urbondo.dagger;

import com.urbondo.api.category.repository.CategoryRepositoryImpl;
import com.urbondo.api.category.service.CategoryService;
import com.urbondo.api.category.service.CategoryServiceImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class CategoryServiceModule {
    @Singleton
    @Provides
    public CategoryService provideCategoryService() {
        return new CategoryServiceImpl(new CategoryRepositoryImpl(DynamoDBConfig.getInstance().getDynamoDBMapper()));
    }
}
