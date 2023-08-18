package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.urbondo.api.repository.UserDao;
import com.urbondo.api.service.AddUserRequestDto;
import com.urbondo.api.service.UserAlreadyFoundException;
import com.urbondo.api.service.UserService;
import com.urbondo.dagger.DaggerUserEndpointComponent;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

import static com.amazonaws.HttpMethod.GET;
import static com.amazonaws.HttpMethod.POST;
import static org.apache.http.HttpStatus.*;

public class UserEndpointsHandler {
    @Inject
    UserService userService;
    @Inject
    Gson gson;
    @Inject
    Validator validator;

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerUserEndpointComponent.create().inject(this);

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            responseEvent = getApiGatewayProxyResponseEvent(requestEvent);
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            responseEvent = postApiGatewayProxyResponseEvent(requestEvent);
        }

        return responseEvent.withHeaders(Collections.singletonMap("content-type", "application/json"));
    }

    private APIGatewayProxyResponseEvent getApiGatewayProxyResponseEvent(APIGatewayProxyRequestEvent requestEvent) {
        try {
            UserDao userDao = userService.findById(requestEvent.getPathParameters().get("id"));
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(gson.toJson(userDao));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND);
        }
    }

    private APIGatewayProxyResponseEvent postApiGatewayProxyResponseEvent(APIGatewayProxyRequestEvent requestEvent) {
        try {
            AddUserRequestDto requestDto = gson.fromJson(requestEvent.getBody(), AddUserRequestDto.class);

            Set<ConstraintViolation<AddUserRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            UserDao userDao = userService.add(requestDto);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_CREATED)
                    .withBody(gson.toJson(userDao));
        } catch (ValidationException | UserAlreadyFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(exception.getMessage());
        }
    }
}
