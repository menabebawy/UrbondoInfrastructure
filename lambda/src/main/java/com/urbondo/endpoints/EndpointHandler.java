package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.urbondo.lib.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

import static org.apache.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public abstract class EndpointHandler {
    @Inject
    Gson gson;
    @Inject
    Validator validator;

    public abstract APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent);

    final APIGatewayProxyResponseEvent badRequest(String message) {
        return failedResponseEvent(SC_BAD_REQUEST, BAD_REQUEST, message);
    }

    final APIGatewayProxyResponseEvent notFound(String message) {
        return failedResponseEvent(SC_NOT_FOUND, NOT_FOUND, message);
    }

    final APIGatewayProxyResponseEvent noContent() {
        return new APIGatewayProxyResponseEvent().withStatusCode(SC_NO_CONTENT);
    }

    final APIGatewayProxyResponseEvent methodNotAllowed() {
        return new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
    }

    final <T> APIGatewayProxyResponseEvent ok(T body) {
        return succeededResponseEvent(SC_OK, body);
    }

    final APIGatewayProxyResponseEvent created(String body) {
        return succeededResponseEvent(SC_CREATED, body);
    }

    final <T> APIGatewayProxyResponseEvent created(T body) {
        return succeededResponseEvent(SC_CREATED, body);
    }

    final <T> void throwConstraintViolationExceptionIfNotValid(T object) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private APIGatewayProxyResponseEvent failedResponseEvent(int statusCode, HttpStatus status, String message) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(gson.toJson(new ErrorResponse(status, message)))
                .withHeaders(Collections.singletonMap("content-type", "application/json"));
    }

    private <T> APIGatewayProxyResponseEvent succeededResponseEvent(int statusCode, T body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(gson.toJson(body))
                .withHeaders(Collections.singletonMap("content-type", "application/json"));
    }
}
