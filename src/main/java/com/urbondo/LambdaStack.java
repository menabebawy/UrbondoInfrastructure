package com.urbondo;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class LambdaStack extends Stack {

    public LambdaStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        new Function(this, "FunctionOne", FunctionProps.builder()
                .runtime(Runtime.JAVA_17)
                .code(Code.fromAsset("lambda/build/libs/lambda-0.1.jar"))
                .handler("com.urbondo.FunctionOne")
                .memorySize(1024)
                .timeout(Duration.seconds(10))
                .logRetention(RetentionDays.ONE_WEEK)
                .build());
    }
}
