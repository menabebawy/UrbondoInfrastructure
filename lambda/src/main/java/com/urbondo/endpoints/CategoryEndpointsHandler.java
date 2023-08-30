package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.JsonSyntaxException;
import com.urbondo.api.category.service.AddCategoryRequestDto;
import com.urbondo.api.category.service.CategoryAlreadyExistException;
import com.urbondo.api.category.service.CategoryService;
import com.urbondo.dagger.DaggerCategoryEndpointComponent;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ValidationException;

import javax.inject.Inject;

import static com.amazonaws.HttpMethod.GET;
import static com.amazonaws.HttpMethod.POST;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

public class CategoryEndpointsHandler extends EndpointHandler {
    @Inject
    CategoryService categoryService;

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerCategoryEndpointComponent.create().inject(this);

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            return getCategory(requestEvent.getPathParameters().get("id"));
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            return addCategory(requestEvent.getBody());
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }
    }

    private APIGatewayProxyResponseEvent getCategory(String id) {
        try {
            return ok(categoryService.findById(id));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_NOT_FOUND);
        }
    }

    private APIGatewayProxyResponseEvent addCategory(String body) {
        try {
            AddCategoryRequestDto requestDto = gson.fromJson(body, AddCategoryRequestDto.class);
            throwConstraintViolationExceptionIfNotValid(requestDto);
            return created(categoryService.add(requestDto.title()));
        } catch (JsonSyntaxException | ValidationException | CategoryAlreadyExistException exception) {
            return badRequest(exception.getMessage());
        }
    }
}