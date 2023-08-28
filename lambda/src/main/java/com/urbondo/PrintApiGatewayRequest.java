package com.urbondo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.urbondo.endpoints.AnnouncementEndpointsHandler;
import com.urbondo.endpoints.CategoryEndpointsHandler;
import com.urbondo.endpoints.EndpointHandler;
import com.urbondo.endpoints.UserEndpointsHandler;

import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;

public class PrintApiGatewayRequest implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        EndpointHandler endpointHandler;

        if (input.getPath().contains("user")) {
            endpointHandler = new UserEndpointsHandler();
        } else if (input.getPath().contains("category")) {
            endpointHandler = new CategoryEndpointsHandler();
        } else if (input.getPath().contains("announcement")) {
            endpointHandler = new AnnouncementEndpointsHandler();
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }

        return endpointHandler.handle(input);
    }
}