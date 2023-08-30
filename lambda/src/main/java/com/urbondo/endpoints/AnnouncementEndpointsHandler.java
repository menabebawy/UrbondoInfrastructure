package com.urbondo.endpoints;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.JsonSyntaxException;
import com.urbondo.api.announcement.service.AddAnnouncementRequestDto;
import com.urbondo.api.announcement.service.AnnouncementNotFoundException;
import com.urbondo.api.announcement.service.AnnouncementService;
import com.urbondo.api.announcement.service.UpdateAnnouncementRequestDto;
import com.urbondo.api.category.service.CategoryNotFoundException;
import com.urbondo.dagger.DaggerAnnouncementEndpointComponent;
import com.urbondo.lib.ResourceNotFoundException;
import jakarta.validation.ValidationException;

import javax.inject.Inject;

import static com.amazonaws.HttpMethod.*;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

public class AnnouncementEndpointsHandler extends EndpointHandler {
    @Inject
    AnnouncementService announcementService;

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        DaggerAnnouncementEndpointComponent.create().inject(this);

        if (requestEvent.getHttpMethod().equals(GET.name())) {
            return getAnnouncement(requestEvent.getPathParameters().get("id"));
        } else if (requestEvent.getHttpMethod().equals(POST.name())) {
            return addAnnouncement(requestEvent.getBody());
        } else if (requestEvent.getHttpMethod().equals(PUT.name())) {
            return updateAnnouncement(requestEvent.getBody());
        } else if (requestEvent.getHttpMethod().equals(DELETE.name())) {
            return deleteAnnouncement(requestEvent.getPathParameters().get("id"));
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_METHOD_NOT_ALLOWED);
        }
    }

    private APIGatewayProxyResponseEvent getAnnouncement(String id) {
        try {
            return ok(announcementService.findById(id));
        } catch (ResourceNotFoundException exception) {
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_NOT_FOUND);
        }
    }

    private APIGatewayProxyResponseEvent addAnnouncement(String body) {
        try {
            AddAnnouncementRequestDto requestDto = gson.fromJson(body, AddAnnouncementRequestDto.class);
            throwConstraintViolationExceptionIfNotValid(requestDto);
            return created(announcementService.add(requestDto));
        } catch (JsonSyntaxException | CategoryNotFoundException | ValidationException exception) {
            return badRequest(exception.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent updateAnnouncement(String body) {
        try {
            UpdateAnnouncementRequestDto requestDto = gson.fromJson(body, UpdateAnnouncementRequestDto.class);
            throwConstraintViolationExceptionIfNotValid(requestDto);
            return ok(announcementService.update(requestDto));
        } catch (JsonSyntaxException | CategoryNotFoundException | AnnouncementNotFoundException |
                 ValidationException exception) {
            return badRequest(exception.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent deleteAnnouncement(String id) {
        try {
            announcementService.deleteById(id);
            return noContent();
        } catch (AnnouncementNotFoundException exception) {
            return notFound(exception.getMessage());
        }
    }
}
