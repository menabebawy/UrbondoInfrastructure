package com.urbondo;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class UrbondoInfrastructureStack extends Stack {
    public UrbondoInfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public UrbondoInfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
    }
}
