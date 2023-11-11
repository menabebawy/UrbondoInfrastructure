package com.urbondo;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.cognito.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceServerScopeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceServerType;
import software.constructs.Construct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static software.amazon.awscdk.Duration.*;
import static software.amazon.awscdk.services.apigateway.MethodLoggingLevel.INFO;
import static software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod.GET;
import static software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod.POST;
import static software.amazon.awscdk.services.cognito.VerificationEmailStyle.CODE;

public class UrbondoStack extends Stack {
    private static final Number READ_CAPACITY = 1;
    private static final String USER = "user";
    private static final String CATEGORY = "category";
    private static final String ANNOUNCEMENT = "announcement";

    private static final String URBONDO_USER_POOL_NAME = "urbondo-user-pool";

    public UrbondoStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);

        UserPool userPool = createCognitoUserPoolAndUserClient();

        Function lambdaFunction = createLambdaFunction();

        Table userTable = createUserTable();
        userTable.grantReadWriteData(lambdaFunction);

        Table categoryTable = createCategoryTable();
        categoryTable.grantReadWriteData(lambdaFunction);

        Table announcementTable = createAnnouncementTable();
        announcementTable.grantReadWriteData(lambdaFunction);

        RestApi restApi = createRestApi(lambdaFunction, userPool);

        new CfnOutput(this,
                      "HttApi",
                      CfnOutputProps.builder().description("Url for Http Api").value(restApi.getUrl()).build());
    }

    private UserPool createCognitoUserPoolAndUserClient() {
        String userClientName = "urbondo-user-pool-client";
        String resourceServerName = "urbondo resource server";

        StandardAttribute required = StandardAttribute.builder().required(true).build();

        StandardAttributes standardAttributes = StandardAttributes.builder()
                .email(required)
                .givenName(required)
                .familyName(required)
                .phoneNumber(required)
                .build();

        PasswordPolicy passwordPolicy = PasswordPolicy.builder()
                .minLength(8)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireSymbols(true)
                .build();

        UserPool userPool = new UserPool(this, URBONDO_USER_POOL_NAME + "_ID", UserPoolProps.builder()
                .userPoolName(URBONDO_USER_POOL_NAME)
                .selfSignUpEnabled(true)
                .standardAttributes(standardAttributes)
                .signInAliases(SignInAliases.builder().email(true).build())
                .passwordPolicy(passwordPolicy)
                .email(UserPoolEmail.withCognito())
                .autoVerify(AutoVerifiedAttrs.builder().email(true).build())
                .userVerification(UserVerificationConfig.builder()
                                          .emailBody("Welcome {####}")
                                          .emailSubject("Welcome to Urbondo")
                                          .emailStyle(CODE)
                                          .build())
                .build());

        new UserPoolClient(this, userClientName + "_ID", UserPoolClientProps.builder()
                .userPoolClientName(userClientName)
                .userPool(userPool)
                .generateSecret(true)
                .accessTokenValidity(minutes(30))
                .refreshTokenValidity(days(30))
                .idTokenValidity(minutes(30))
                .authFlows(AuthFlow.builder().userPassword(true).build())
                .oAuth(OAuthSettings.builder()
                               .flows(OAuthFlows.builder().implicitCodeGrant(true).build())
                               .build())
                .build());

        ResourceServerType.builder()
                .name(resourceServerName)
                .scopes(Collections.singletonList(ResourceServerScopeType.builder()
                                                          .scopeName("openid")
                                                          .scopeDescription("Read profile")
                                                          .build()))
                .identifier("https://lt1chwlaef.execute-api.us-east-1.amazonaws.com")
                .userPoolId(userPool.getUserPoolId())
                .build();

        return userPool;
    }

    private Function createLambdaFunction() {
        return new Function(this,
                            "ApiGatewayRequestHandlerID",
                            FunctionProps.builder()
                                    .runtime(Runtime.JAVA_17)
                                    .code(Code.fromAsset("../lambda/build/distributions/lambda-0.1.zip"))
                                    .handler("com.urbondo.ApiGatewayRequestHandler")
                                    .memorySize(1024)
                                    .timeout(seconds(10))
                                    .build());
    }

    private RestApi createRestApi(Function lambdaFunction, UserPool userPool) {
        Map<String, Boolean> idParam = new HashMap<>();
        idParam.put("method.request.querystring.id", true);

        RestApiProps restApiProps = RestApiProps.builder()
                .restApiName("Urbondo-REST-Api")
                .deployOptions(StageOptions.builder().loggingLevel(INFO).build())
                .build();

        RestApi restApi = new RestApi(this, "Urbondo-REST", restApiProps);

        CognitoUserPoolsAuthorizerProps cognitoUserPoolsAuthorizerProps = CognitoUserPoolsAuthorizerProps.builder()
                .authorizerName("urbondo-cognito-authorizer")
                .cognitoUserPools(Collections.singletonList(userPool))
                .build();

        Authorizer authorizer = new CognitoUserPoolsAuthorizer(this,
                                                               "urbondo-cognito-authorizer",
                                                               cognitoUserPoolsAuthorizerProps);

        MethodOptions methodOptions = MethodOptions.builder()
                .authorizer(authorizer)
                .build();

        LambdaIntegration lambdaIntegration = LambdaIntegration.Builder.create(lambdaFunction).build();

        ResourceOptions resourceOptions = ResourceOptions.builder()
                .defaultIntegration(lambdaIntegration)
                .defaultMethodOptions(methodOptions)
                .build();

        ResourceOptions userResourceOptions = ResourceOptions.builder()
                .defaultIntegration(lambdaIntegration)
                .build();

        RequestValidatorOptions queryParamRequestValidatorOptions = RequestValidatorOptions.builder()
                .validateRequestParameters(true)
                .build();

        Resource userResource = restApi.getRoot().addResource(USER, userResourceOptions);
        userResource.addResource("signup").addMethod(POST.name());
        userResource.addResource("login").addMethod(POST.name());
        userResource.addResource("refreshtoken").addMethod(POST.name());
        userResource.addResource("confirm").addMethod(POST.name());
        userResource.addResource("resendcode").addMethod(POST.name());
        userResource.addResource("{id}")
                .addMethod(GET.name(),
                           lambdaIntegration,
                           MethodOptions.builder()
                                   .requestValidatorOptions(queryParamRequestValidatorOptions)
                                   .requestParameters(idParam).build());

        Resource categoryResource = restApi.getRoot().addResource(CATEGORY, resourceOptions);
        categoryResource.addMethod(POST.name());
        categoryResource.addResource("{id}")
                .addMethod(GET.name(),
                           lambdaIntegration,
                           MethodOptions.builder()
                                   .authorizer(authorizer)
                                   .requestValidatorOptions(queryParamRequestValidatorOptions)
                                   .requestParameters(idParam).build());

        Resource announcementResource = restApi.getRoot().addResource(ANNOUNCEMENT, resourceOptions);
        announcementResource.addMethod(POST.name());
        announcementResource.addResource("{id}")
                .addMethod(GET.name(),
                           lambdaIntegration,
                           MethodOptions.builder()
                                   .authorizer(authorizer)
                                   .requestValidatorOptions(queryParamRequestValidatorOptions)
                                   .requestParameters(idParam).build());

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