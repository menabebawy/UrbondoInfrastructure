package com.urbondo;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class UrbondoApp {
    public static void main(final String[] args) {
        App app = new App();

        StackProps stackProps = StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build();

        new DynamoDbStack(app, DynamoDbStack.class.getSimpleName(), stackProps);

        new LambdaStack(app, LambdaStack.class.getSimpleName(), stackProps);

        new ApiGatewayStack(app, ApiGatewayStack.class.getSimpleName(), stackProps);

        app.synth();
    }
}