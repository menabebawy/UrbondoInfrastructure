package com.urbondo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;
import java.util.Collections;

public class PrintApiGatewayRequest implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    DynamoDBMapper dynamoDBMapper;

    private static APIGatewayProxyResponseEvent badRequest() {
        return new APIGatewayProxyResponseEvent().withStatusCode(400);
    }

    private static APIGatewayProxyResponseEvent success(String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(body)
                .withHeaders(Collections.singletonMap("content-type", "text/plain"));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LambdaLogger logger = context.getLogger();
        logger.log("Method Start " + input);
        logger.log("CONTEXT: " + gson.toJson(context));

        //Initialize response and DB
        logger.log("Inizialize response and DB ");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            logger.log("Getting REST method type ");
            String restMethod = input.getHttpMethod();
            logger.log("REST method type " + restMethod);
            logger.log("Input body: " + input.getBody());
            logger.log("Input path " + input.getPath());

            if (restMethod.equals("GET")) {
                logger.log("GET method Start ");
                UserDao userDao = getUserById(input.getPathParameters().get("id"));
                return success(new Gson().toJson(userDao));
            }
        } catch (Exception e) {
            response.setStatusCode(405);
            response.setBody("exception Thrown");
            logger.log("Exception: " + e);
        }

        return badRequest();
    }

    private UserDao getUserById(String id) {
        HandlerComponent handlerComponent = DaggerHandlerComponent.create();
        handlerComponent.inject(this);


        System.out.println("Just injected DynamoDB *******");

        System.out.println("Request now: " + id);

        UserDao userDao = dynamoDBMapper.load(UserDao.class, id);
        if (userDao == null) {

        }
        return userDao;
    }
}