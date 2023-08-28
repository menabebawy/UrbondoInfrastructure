package com.urbondo.api.announcement.service;

public class AnnouncementNotFoundException extends RuntimeException {
    public AnnouncementNotFoundException(String id) {
        super("announcement id: " + id + " not found.");
    }
}
