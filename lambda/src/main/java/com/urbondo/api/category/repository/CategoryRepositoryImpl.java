package com.urbondo.api.category.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final DynamoDBMapper dynamoDBMapper;

    @Inject
    public CategoryRepositoryImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Optional<CategoryDao> findById(String id) {
        CategoryDao categoryDao = dynamoDBMapper.load(CategoryDao.class, id);
        if (categoryDao == null) {
            return Optional.empty();
        }
        return Optional.of(categoryDao);
    }

    @Override
    public Optional<CategoryDao> findByTitle(String title) {
        Map<String, AttributeValue> attributesValues = new HashMap<>();
        attributesValues.put(":title", new AttributeValue().withS(title));

        Map<String, String> attributesNames = new HashMap<>();
        attributesNames.put("#title", "title");

        DynamoDBQueryExpression<CategoryDao> queryExpression = new DynamoDBQueryExpression<CategoryDao>()
                .withIndexName("title-index")
                .withKeyConditionExpression("#title = :title")
                .withExpressionAttributeNames(attributesNames)
                .withExpressionAttributeValues(attributesValues)
                .withConsistentRead(false);

        return dynamoDBMapper.query(CategoryDao.class, queryExpression).stream().findFirst();
    }

    @Override
    public CategoryDao save(CategoryDao categoryDao) {
        dynamoDBMapper.save(categoryDao);
        return categoryDao;
    }

    @Override
    public void delete(CategoryDao categoryDao) {
        dynamoDBMapper.delete(categoryDao);
    }
}
