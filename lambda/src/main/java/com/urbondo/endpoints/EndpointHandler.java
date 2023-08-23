package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface EndpointHandler {
    APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent);
}
