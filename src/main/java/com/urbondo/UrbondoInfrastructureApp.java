package com.urbondo;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class UrbondoInfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        new UrbondoInfrastructureStack(app, "UrbondoInfrastructureStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        app.synth();
    }
}