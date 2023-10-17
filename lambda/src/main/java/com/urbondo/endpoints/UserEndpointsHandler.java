package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.JsonSyntaxException;
import com.urbondo.api.user.service.*;
import com.urbondo.dagger.DaggerUserEndpointComponent;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import javax.inject.Inject;
import javax.inject.Named;

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
    @Named("clientId")
    String clientId;

    @Inject
    @Named("clientSecret")
    String clientSecret;

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
            try {
                return created(cognitoService.signup(requestDto, clientId, clientSecret));
            } catch (AwsServiceException awsServiceException) {
                return badRequest(awsServiceException.getLocalizedMessage());
            }
        } else if (requestEvent.getPath().contains("login")) {
            LoginRequestDtp requestDto = gson.fromJson(requestEvent.getBody(), LoginRequestDtp.class);
            try {
                return ok(cognitoService.initiateAuth(clientId,
                                                      clientSecret,
                                                      requestDto.username(),
                                                      requestDto.password()));
            } catch (AwsServiceException awsServiceException) {
                return badRequest(awsServiceException.getLocalizedMessage());
            }
        } else {
            return methodNotAllowed();
        }
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
