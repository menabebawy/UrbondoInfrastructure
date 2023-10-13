package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.urbondo.api.user.service.*;
import com.urbondo.dagger.DaggerUserEndpointComponent;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;

import static com.amazonaws.HttpMethod.*;
import static com.amazonaws.services.dynamodbv2.model.AttributeAction.PUT;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

public class UserEndpointsHandler extends EndpointHandler {
    @Inject
    UserService userService;

    @Inject
    CognitoService cognitoService;

    @Inject
    Gson gson;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerUserEndpointComponent.create().inject(this);

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            return getUser(requestEvent.getPathParameters().get("id"));
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            // here we expect either signup or login
            return userRegistration(requestEvent);
        } else if (requestEvent.getHttpMethod().equals(PUT.name())) {
            return updateUser(requestEvent.getBody());
        } else if (requestEvent.getHttpMethod().equals(DELETE.name())) {
            return deleteUser(requestEvent.getPathParameters().get("id"));
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }
    }

    private APIGatewayProxyResponseEvent getUser(String id) {
        try {
            return ok(userService.findById(id));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_NOT_FOUND);
        }
    }

    private APIGatewayProxyResponseEvent userRegistration(APIGatewayProxyRequestEvent requestEvent) {
        if (requestEvent.getPath().contains("signup")) {
            SignupRequestDto requestDto = gson.fromJson(requestEvent.getBody(), SignupRequestDto.class);
            JsonObject response = cognitoService.signup(requestDto, clientId, clientSecret);
            return created(response);
        } else if (requestEvent.getPath().contains("login")) {
            return methodNotAllowed();
        } else {
            return methodNotAllowed();
        }
//        try {
//            AddUserRequestDto requestDto = gson.fromJson(body, AddUserRequestDto.class);
//            throwConstraintViolationExceptionIfNotValid(requestDto);
//            return created(userService.add(requestDto));
//        } catch (JsonSyntaxException | ValidationException | UserAlreadyFoundException exception) {
//            return badRequest(exception.getMessage());
//        }
    }

    private APIGatewayProxyResponseEvent updateUser(String body) {
        try {
            UpdateUserRequestDto requestDto = gson.fromJson(body, UpdateUserRequestDto.class);
            throwConstraintViolationExceptionIfNotValid(requestDto);
            return ok(userService.update(requestDto));
        } catch (JsonSyntaxException | ValidationException exception) {
            return badRequest(exception.getMessage());
        } catch (UserNotFoundException exception) {
            return notFound(exception.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent deleteUser(String id) {
        try {
            userService.deleteBy(id);
            return noContent();
        } catch (UserNotFoundException exception) {
            return notFound(exception.getMessage());
        }
    }
}
