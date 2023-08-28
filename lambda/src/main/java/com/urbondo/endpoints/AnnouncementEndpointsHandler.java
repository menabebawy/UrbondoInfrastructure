package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.urbondo.api.announcement.repositoy.AnnouncementDao;
import com.urbondo.api.announcement.service.AddAnnouncementRequestDto;
import com.urbondo.api.announcement.service.AnnouncementService;
import com.urbondo.api.announcement.service.UpdateAnnouncementRequestDto;
import com.urbondo.api.category.service.CategoryNotFoundException;
import com.urbondo.dagger.DaggerAnnouncementEndpointComponent;
import com.urbondo.lib.ErrorResponse;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import javax.inject.Inject;
import java.util.Set;

import static com.amazonaws.HttpMethod.*;
import static org.apache.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class AnnouncementEndpointsHandler implements EndpointHandler {
    @Inject
    AnnouncementService announcementService;
    @Inject
    Gson gson;
    @Inject
    Validator validator;

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerAnnouncementEndpointComponent.create().inject(this);

        APIGatewayProxyResponseEvent responseEvent;

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            responseEvent = getAnnouncement(requestEvent.getPathParameters().get("id"));
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            responseEvent = addAnnouncement(requestEvent.getBody());
        } else if (requestEvent.getHttpMethod().equals(PUT.name())) {
            responseEvent = updateAnnouncement(requestEvent.getBody());
        } else if (requestEvent.getHttpMethod().equals(DELETE.name())) {
            responseEvent = deleteAnnouncement(requestEvent.getPathParameters().get("id"));
        } else {
            responseEvent = new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }

        return responseEvent;
    }

    private APIGatewayProxyResponseEvent getAnnouncement(String id) {
        try {
            AnnouncementDao announcementDao = announcementService.findById(id);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(gson.toJson(announcementDao));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND)
                    .withBody(gson.toJson(new ErrorResponse(NOT_FOUND, exception.getMessage())));
        }
    }

    private APIGatewayProxyResponseEvent addAnnouncement(String body) {
        try {
            AddAnnouncementRequestDto requestDto = gson.fromJson(body, AddAnnouncementRequestDto.class);
            Set<ConstraintViolation<AddAnnouncementRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            AnnouncementDao announcementDao = announcementService.add(requestDto);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_CREATED)
                    .withBody(gson.toJson(announcementDao));

        } catch (CategoryNotFoundException | ValidationException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(gson.toJson(new ErrorResponse(BAD_REQUEST, exception.getMessage())));
        }
    }

    private APIGatewayProxyResponseEvent updateAnnouncement(String body) {
        try {
            UpdateAnnouncementRequestDto requestDto = gson.fromJson(body, UpdateAnnouncementRequestDto.class);
            Set<ConstraintViolation<UpdateAnnouncementRequestDto>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            AnnouncementDao announcementDao = announcementService.update(requestDto);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(gson.toJson(announcementDao));
        } catch (CategoryNotFoundException | ValidationException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(gson.toJson(new ErrorResponse(BAD_REQUEST, exception.getMessage())));
        }
    }

    private APIGatewayProxyResponseEvent deleteAnnouncement(String id) {
        try {
            announcementService.deleteById(id);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NO_CONTENT);
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_NOT_FOUND)
                    .withBody(gson.toJson(new ErrorResponse(NOT_FOUND, exception.getMessage())));
        }
    }
}
