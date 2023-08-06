package com.urbondo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import static com.amazonaws.regions.Regions.US_EAST_1;

@Module
public class DynamoDBModule {

    @Provides
    @Singleton
    DynamoDBMapper providesDynamoDBMapper() {
        return new DynamoDBMapper(amazonDynamoDB(awsCredentials()));
    }

    private AmazonDynamoDB amazonDynamoDB(AWSCredentials awsCredentials) {
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(US_EAST_1)
                .build();

    }

    private AWSCredentials awsCredentials() {
        return new DefaultAWSCredentialsProviderChain().getCredentials();
    }
}
