package com.urbondo;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.constructs.Construct;

import static software.amazon.awscdk.services.apigateway.MethodLoggingLevel.INFO;
import static software.amazon.awscdk.services.logs.RetentionDays.ONE_WEEK;
import static software.amazon.awssdk.regions.Region.US_EAST_1;

public class UrbondoStack extends Stack {
    private static final Number READ_CAPACITY = 1;
    private static final Logger logger = LoggerFactory.getLogger(UrbondoStack.class);

    public UrbondoStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        CognitoIdentityProviderClient cognitoClient = cognitoProviderClient();
        String userPoolId = createUserPool(cognitoClient).userPool().id();
        createUserPoolClient(cognitoClient, userPoolId);

        Function lambdaFunction = createLambdaFunction();

        Table userTable = createUserTable();
        userTable.grantReadWriteData(lambdaFunction);

        Table categoryTable = createCategoryTable();
        categoryTable.grantReadWriteData(lambdaFunction);

        Table announcementTable = createAnnouncementTable();
        announcementTable.grantReadWriteData(lambdaFunction);

        RestApi restApi = createRestApi(lambdaFunction);

        new CfnOutput(this, "HttApi", CfnOutputProps.builder()
                .description("Url for Http Api")
                .value(restApi.getUrl())
                .build());
    }

    private Function createLambdaFunction() {
        return new Function(this, "PrintRequest",
                FunctionProps.builder()
                        .runtime(Runtime.JAVA_17)
                        .code(Code.fromAsset("../lambda/build/distributions/lambda-0.1.zip"))
                        .handler("com.urbondo.PrintApiGatewayRequest")
                        .memorySize(1024)
                        .timeout(Duration.seconds(10))
                        .logRetention(ONE_WEEK)
                        .build());
    }

    private RestApi createRestApi(Function lambdaFunction) {
        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName("Urbondo-REST-Api")
                .deployOptions(StageOptions.builder()
                        .loggingLevel(INFO)
                        .build())
                .build();

        RestApi restApi = new RestApi(this, "Urbondo-REST", restApiProps);

        ResourceOptions resourceOptions = ResourceOptions.builder()
                .defaultIntegration(LambdaIntegration.Builder.create(lambdaFunction).build())
                .build();

        addResource(restApi.getRoot(), "user", resourceOptions);
        addResource(restApi.getRoot(), "announcement", resourceOptions);
        addResource(restApi.getRoot(), "category", resourceOptions);

        return restApi;
    }


    private static void addResource(IResource rootResource, String path, ResourceOptions resourceOptions) {
        rootResource.addResource(path, resourceOptions)
                .addMethod(HttpMethod.POST.name())
                .getResource()
                .addResource("{id}")
                .addMethod(HttpMethod.ANY.name());
    }

    private Table createUserTable() {
        Table userTable = createTableBySuffix("user");
        userTable.addGlobalSecondaryIndex(globalSecondaryIndexProps("email"));
        return userTable;
    }

    private Table createAnnouncementTable() {
        return createTableBySuffix("announcement");
    }

    private Table createCategoryTable() {
        Table categoryTable = createTableBySuffix("category");
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

    private static CognitoIdentityProviderClient cognitoProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    private static CreateUserPoolResponse createUserPool(CognitoIdentityProviderClient cognitoClient) {
        try {
            CreateUserPoolRequest poolRequest = CreateUserPoolRequest.builder()
                    .poolName("urbondo-user-pool")
                    .build();
            return cognitoClient.createUserPool(poolRequest);
        } catch (CognitoIdentityProviderException exception) {
            logger.error(exception.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return null;
    }

    private static void createUserPoolClient(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
        try {
            CreateUserPoolClientRequest poolClientRequest = CreateUserPoolClientRequest.builder()
                    .userPoolId(userPoolId)
                    .clientName("urbondo-user-pool-client")
                    .generateSecret(true)
                    .build();
            cognitoClient.createUserPoolClient(poolClientRequest);
        } catch (CognitoIdentityProviderException exception) {
            logger.error(exception.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}