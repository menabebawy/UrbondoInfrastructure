package com.urbondo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DynamoDBModule.class})
public interface HandlerComponent {
    void inject(PrintApiGatewayRequest printApiGatewayRequest);

    DynamoDBMapper getDynamoDB();
}