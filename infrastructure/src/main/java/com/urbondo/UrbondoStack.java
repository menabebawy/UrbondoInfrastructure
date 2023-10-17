package com.urbondo;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import static software.amazon.awscdk.services.apigateway.MethodLoggingLevel.INFO;
import static software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod.GET;
import static software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod.POST;
import static software.amazon.awscdk.services.logs.RetentionDays.ONE_WEEK;

public class UrbondoStack extends Stack {
    private static final Number READ_CAPACITY = 1;
    private static final String USER = "user";
    private static final String CATEGORY = "category";
    private static final String ANNOUNCEMENT = "announcement";

    public UrbondoStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        Function lambdaFunction = createLambdaFunction();

        Table userTable = createUserTable();
        userTable.grantReadWriteData(lambdaFunction);

        Table categoryTable = createCategoryTable();
        categoryTable.grantReadWriteData(lambdaFunction);

        Table announcementTable = createAnnouncementTable();
        announcementTable.grantReadWriteData(lambdaFunction);

        RestApi restApi = createRestApi(lambdaFunction);

        new CfnOutput(this,
                      "HttApi",
                      CfnOutputProps.builder().description("Url for Http Api").value(restApi.getUrl()).build());
    }

    private Function createLambdaFunction() {
        return new Function(this,
                            "PrintRequest",
                            FunctionProps.builder()
                                    .runtime(Runtime.JAVA_17)
                                    .code(Code.fromAsset("../lambda/build/distributions/lambda-0.1.zip"))
                                    .handler("com.urbondo.ApiGatewayRequestHandler")
                                    .memorySize(1024)
                                    .timeout(Duration.seconds(10))
                                    .logRetention(ONE_WEEK)
                                    .build());
    }

    private RestApi createRestApi(Function lambdaFunction) {
        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName("Urbondo-REST-Api")
                .deployOptions(StageOptions.builder().loggingLevel(INFO).build())
                .build();

        RestApi restApi = new RestApi(this, "Urbondo-REST", restApiProps);

        ResourceOptions resourceOptions = ResourceOptions.builder()
                .defaultIntegration(LambdaIntegration.Builder.create(lambdaFunction).build())
                .build();

        Resource userResource = restApi.getRoot()
                .addResource(USER, resourceOptions);
        userResource.addResource("signup").addMethod(POST.name());
        userResource.addResource("login").addMethod(POST.name());
        userResource.addResource("{id}").addMethod(GET.name());

        Resource categoryResource = restApi.getRoot()
                .addResource(CATEGORY, resourceOptions);
        categoryResource.addMethod(POST.name());
        categoryResource.addResource("{id}").addMethod(GET.name());

        Resource announcementResource = restApi.getRoot()
                .addResource(ANNOUNCEMENT, resourceOptions);
        announcementResource.addMethod(POST.name());
        announcementResource.addResource("{id}").addMethod(GET.name());

        return restApi;
    }

    private Table createUserTable() {
        Table userTable = createTableBySuffix(USER);
        userTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("email"));
        return userTable;
    }

    private Table createAnnouncementTable() {
        return createTableBySuffix(ANNOUNCEMENT);
    }

    private Table createCategoryTable() {
        Table categoryTable = createTableBySuffix(CATEGORY);
        categoryTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("title"));
        return categoryTable;
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

    private static GlobalSecondaryIndexProps globalSecondaryIndexProps(String attributeName) {
        return GlobalSecondaryIndexProps.builder()
                .indexName(attributeName + "-index")
                .partitionKey(Attribute.builder().name(attributeName).type(AttributeType.STRING).build())
                .readCapacity(READ_CAPACITY)
                .writeCapacity(READ_CAPACITY)
                .build();
    }
}