package com.urbondo;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.*;
import software.constructs.Construct;

public class DynamoDbStack extends Stack {
    private static final Number READ_CAPACITY = 1;

    public DynamoDbStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        Table userTable = createTableBySuffix("user");
        userTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("email"));

        Table categoryTable = createTableBySuffix("category");
        categoryTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("title"));

        createTableBySuffix("announcement");
    }

    private Table createTableBySuffix(String name) {
        String prefix = "urbondo";
        String idAttributeName = "id";

        TableProps tableProps = TableProps.builder()
                .partitionKey(Attribute.builder().name(idAttributeName).type(AttributeType.STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .removalPolicy(RemovalPolicy.DESTROY)
                .tableName(prefix + "-" + name)
                .build();

        return new Table(this, name, tableProps);
    }

    private GlobalSecondaryIndexProps globalSecondaryIndexProps(String attributeName) {
        return GlobalSecondaryIndexProps.builder()
                .indexName(attributeName + "-index")
                .partitionKey(Attribute.builder().name(attributeName).type(AttributeType.STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .build();
    }
}
