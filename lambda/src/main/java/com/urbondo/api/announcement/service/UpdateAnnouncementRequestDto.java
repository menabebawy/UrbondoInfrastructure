package com.urbondo.api.announcement.service;

import jakarta.validation.constraints.NotBlank;

public class UpdateAnnouncementRequestDto extends AnnouncementRequestDto {
    @NotBlank(message = "id must not blank")
    private final String id;

    public UpdateAnnouncementRequestDto(String id, String title, String body, String categoryId) {
        super(title, body, categoryId);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
