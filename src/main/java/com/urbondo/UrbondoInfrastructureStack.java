package com.urbondo;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.constructs.Construct;

import static software.amazon.awscdk.RemovalPolicy.DESTROY;
import static software.amazon.awscdk.services.dynamodb.AttributeType.STRING;

public class UrbondoInfrastructureStack extends Stack {
    private static final Number READ_CAPACITY = 1;

    public UrbondoInfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);


        Table userTable = createTable("user");
        userTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("email"));

        Table categoryTable = createTable("category");
        categoryTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("title"));

        createTable("announcement");
    }

    private Table createTable(String name) {
        String prefix = "urbondo-";
        String idAttributeName = "id";

        TableProps tableProps = TableProps.builder()
                .partitionKey(Attribute.builder().name(idAttributeName).type(STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .removalPolicy(DESTROY)
                .tableName(prefix + name)
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
