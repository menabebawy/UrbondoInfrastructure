package com.urbondo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.urbondo.endpoints.CategoryEndpointsHandler;
import com.urbondo.endpoints.EndpointHandler;
import com.urbondo.endpoints.UserEndpointsHandler;

public class PrintApiGatewayRequest implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        EndpointHandler endpointHandler;

        if (input.getPath().contains("user")) {
            endpointHandler = new UserEndpointsHandler();
        } else {
            endpointHandler = new CategoryEndpointsHandler();
        }

        return endpointHandler.handle(input);
    }
}