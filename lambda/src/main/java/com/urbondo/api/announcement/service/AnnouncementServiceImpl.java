package com.urbondo.api.announcement.service;

import com.urbondo.api.announcement.repositoy.AnnouncementDao;
import com.urbondo.api.category.repository.CategoryDao;
import com.urbondo.api.category.repository.CategoryRepository;
import com.urbondo.api.category.service.CategoryNotFoundException;
import com.urbondo.lib.ResourceNotFoundException;
import com.urbondo.lib.UrbondoRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class AnnouncementServiceImpl implements AnnouncementService {
    private final UrbondoRepository<AnnouncementDao> announcementRepository;
    private final CategoryRepository categoryRepository;

    @Inject
    public AnnouncementServiceImpl(UrbondoRepository<AnnouncementDao> announcementRepository,
                                   CategoryRepository categoryRepository) {
        this.announcementRepository = announcementRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public AnnouncementDao findById(String id) {
        Optional<AnnouncementDao> announcementDAO = announcementRepository.findById(id);

        if (announcementDAO.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        return announcementDAO.get();
    }

    @Override
    public AnnouncementDao add(AddAnnouncementRequestDto requestDTO) {
        CategoryDao categoryDAO = getCategoryDaoOrThrowException(requestDTO.getCategoryId());

        AnnouncementDao announcementDAO = new AnnouncementDao(UUID.randomUUID().toString(),
                                                              requestDTO.getTitle(),
                                                              requestDTO.getBody(),
                                                              requestDTO.getCategoryId(),
                                                              categoryDAO.getTitle(),
                                                              requestDTO.getUserId());
        return announcementRepository.save(announcementDAO);
    }

    private CategoryDao getCategoryDaoOrThrowException(String id) {
        Optional<CategoryDao> categoryDAO = categoryRepository.findById(id);

        if (categoryDAO.isEmpty()) {
            throw new CategoryNotFoundException(id);
        }
        return categoryDAO.get();
    }

    @Override
    public AnnouncementDao update(UpdateAnnouncementRequestDto requestDTO) {
        AnnouncementDao storedAnnouncementDao = findByIdOrThrowException(requestDTO.getId());
        CategoryDao categoryDAO = getCategoryDaoOrThrowException(requestDTO.getCategoryId());

        storedAnnouncementDao.setTitle(requestDTO.getTitle());
        storedAnnouncementDao.setBody(requestDTO.getBody());
        storedAnnouncementDao.setCategoryId(requestDTO.getCategoryId());
        storedAnnouncementDao.setCategoryTitle(categoryDAO.getTitle());

        announcementRepository.save(storedAnnouncementDao);

        return storedAnnouncementDao;
    }

    private AnnouncementDao findByIdOrThrowException(String id) {
        Optional<AnnouncementDao> announcementDAO = announcementRepository.findById(id);

        if (announcementDAO.isEmpty()) {
            throw new AnnouncementNotFoundException(id);
        }

        return announcementDAO.get();
    }

    @Override
    public void deleteById(String id) {
        announcementRepository.delete(findByIdOrThrowException(id));
    }
}
