package com.urbondo;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class UrbondoApp {

    public static void main(final String[] args) {
        App app = new App();

        StackProps props = StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build();

        new UrbondoStack(app, UrbondoStack.class.getSimpleName(), props);

        new CognitoStack(app, CognitoStack.class.getSimpleName(), props);

        app.synth();
    }
}