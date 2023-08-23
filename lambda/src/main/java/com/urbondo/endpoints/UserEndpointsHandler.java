package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.urbondo.api.repository.UserDao;
import com.urbondo.api.service.*;
import com.urbondo.dagger.DaggerUserEndpointComponent;
import com.urbondo.lib.ErrorResponse;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

import static com.amazonaws.HttpMethod.*;
import static com.amazonaws.services.dynamodbv2.model.AttributeAction.PUT;
import static org.apache.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class UserEndpointsHandler {
    @Inject
    UserService userService;
    @Inject
    Gson gson;
    @Inject
    Validator validator;

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerUserEndpointComponent.create().inject(this);

        APIGatewayProxyResponseEvent responseEvent;

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            responseEvent = getUser(requestEvent);
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            responseEvent = addNewUser(requestEvent);
        } else if (requestEvent.getHttpMethod().equals(PUT.name())) {
            responseEvent = updateUser(requestEvent);
        } else if (requestEvent.getHttpMethod().equals(DELETE.name())) {
            responseEvent = deleteUser(requestEvent);
        } else {
            responseEvent = new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }

        return responseEvent.withHeaders(Collections.singletonMap("content-type", "application/json"));
    }

    private APIGatewayProxyResponseEvent getUser(APIGatewayProxyRequestEvent requestEvent) {
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

    private APIGatewayProxyResponseEvent addNewUser(APIGatewayProxyRequestEvent requestEvent) {
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
                    .withBody(gson.toJson(new ErrorResponse(BAD_REQUEST, exception.getMessage())));
        }
    }

    private APIGatewayProxyResponseEvent updateUser(APIGatewayProxyRequestEvent requestEvent) {
        try {
            UpdateUserRequestDto requestDto = gson.fromJson(requestEvent.getBody(), UpdateUserRequestDto.class);

            Set<ConstraintViolation<UpdateUserRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            UserDao userDao = userService.update(requestDto);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(gson.toJson(userDao));
        } catch (ValidationException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(gson.toJson(new ErrorResponse(BAD_REQUEST, exception.getMessage())));
        } catch (UserNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND)
                    .withBody(gson.toJson(new ErrorResponse(NOT_FOUND, exception.getMessage())));
        }
    }

    private APIGatewayProxyResponseEvent deleteUser(APIGatewayProxyRequestEvent requestEvent) {
        try {
            userService.deleteBy(requestEvent.getPathParameters().get("id"));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NO_CONTENT);
        } catch (UserNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND)
                    .withBody(gson.toJson(new ErrorResponse(NOT_FOUND, exception.getMessage())));
        }
    }
}
