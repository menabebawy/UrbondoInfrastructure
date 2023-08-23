package com.urbondo.api.user.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final DynamoDBMapper dynamoDBMapper;

    @Inject
    public UserRepositoryImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Optional<UserDao> findById(String id) {
        UserDao userDAO = dynamoDBMapper.load(UserDao.class, id);
        if (userDAO == null) {
            return Optional.empty();
        }
        return Optional.of(userDAO);
    }

    @Override
    public boolean isEmailExist(String email) {
        Map<String, AttributeValue> attributesValues = new HashMap<>();
        attributesValues.put(":email", new AttributeValue().withS(email));

        Map<String, String> attributesNames = new HashMap<>();
        attributesNames.put("#email", "email");

        DynamoDBQueryExpression<UserDao> queryExpression = new DynamoDBQueryExpression<UserDao>().withIndexName("email-index")
                .withKeyConditionExpression("#email = :email")
                .withExpressionAttributeNames(attributesNames)
                .withExpressionAttributeValues(attributesValues)
                .withConsistentRead(false);

        return !dynamoDBMapper.query(UserDao.class, queryExpression).isEmpty();
    }

    @Override
    public UserDao save(UserDao userDAO) {
        dynamoDBMapper.save(userDAO);
        return userDAO;
    }

    @Override
    public void delete(UserDao userDAO) {
        dynamoDBMapper.delete(userDAO);
    }
}
