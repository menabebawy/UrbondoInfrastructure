package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.urbondo.api.category.repository.CategoryDao;
import com.urbondo.api.category.service.AddCategoryRequestDto;
import com.urbondo.api.category.service.CategoryAlreadyExistException;
import com.urbondo.api.category.service.CategoryService;
import com.urbondo.dagger.DaggerCategoryEndpointComponent;
import com.urbondo.lib.ErrorResponse;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class CategoryEndpointsHandler implements EndpointHandler {
    @Inject
    CategoryService categoryService;
    @Inject
    Gson gson;
    @Inject
    Validator validator;

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerCategoryEndpointComponent.create().inject(this);

        APIGatewayProxyResponseEvent responseEvent;

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            responseEvent = getCategory(requestEvent.getPathParameters().get("id"));
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            responseEvent = addCategory(requestEvent.getBody());
        } else {
            responseEvent = new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }

        return responseEvent.withHeaders(Collections.singletonMap("content-type", "application/json"));
    }

    private APIGatewayProxyResponseEvent getCategory(String id) {
        try {
            CategoryDao categoryDao = categoryService.findById(id);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(gson.toJson(categoryDao));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND);
        }
    }

    private APIGatewayProxyResponseEvent addCategory(String body) {
        try {
            AddCategoryRequestDto requestDto = gson.fromJson(body, AddCategoryRequestDto.class);
            Set<ConstraintViolation<AddCategoryRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            CategoryDao categoryDao = categoryService.add(requestDto.title());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_CREATED)
                    .withBody(gson.toJson(categoryDao));
        } catch (ValidationException | CategoryAlreadyExistException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(gson.toJson(new ErrorResponse(BAD_REQUEST, exception.getMessage())));
        }
    }
}