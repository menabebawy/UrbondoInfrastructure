package com.urbondo.api.announcement.service;

import com.urbondo.api.announcement.repositoy.AnnouncementDao;

public interface AnnouncementService {
    AnnouncementDao findById(String id);

    AnnouncementDao add(AddAnnouncementRequestDto requestDTO);

    AnnouncementDao update(UpdateAnnouncementRequestDto requestDTO);

    void deleteById(String id);
}
