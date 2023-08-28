package com.urbondo.api.announcement.service;

import jakarta.validation.constraints.NotBlank;

public class AddAnnouncementRequestDto extends AnnouncementRequestDto {
    @NotBlank(message = "user id must not blank")
    private final String userId;

    public AddAnnouncementRequestDto(String title,
                                     String body,
                                     String categoryId,
                                     String userId) {
        super(title, body, categoryId);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
