package com.urbondo;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.constructs.Construct;

import static software.amazon.awscdk.RemovalPolicy.DESTROY;
import static software.amazon.awscdk.services.dynamodb.AttributeType.STRING;

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
                .partitionKey(Attribute.builder().name(idAttributeName).type(STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .removalPolicy(DESTROY)
                .tableName(prefix + "-" + name)
                .build();

        return new Table(this, name, tableProps);
    }

    private GlobalSecondaryIndexProps globalSecondaryIndexProps(String attributeName) {
        return GlobalSecondaryIndexProps.builder()
                .indexName(attributeName + "-index")
                .partitionKey(Attribute.builder().name(attributeName).type(STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .build();
    }
}
