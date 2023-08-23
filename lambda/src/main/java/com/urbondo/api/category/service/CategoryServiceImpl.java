package com.urbondo.api.category.service;

import com.urbondo.api.category.repository.CategoryDao;
import com.urbondo.api.category.repository.CategoryRepository;
import com.urbondo.lib.ResourceNotFoundException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Inject
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDao findById(String id) {
        Optional<CategoryDao> categoryDAO = categoryRepository.findById(id);
        if (categoryDAO.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        return categoryDAO.get();
    }

    @Override
    public CategoryDao add(String title) {
        if (categoryRepository.findByTitle(title).isPresent()) {
            throw new CategoryAlreadyExistException(title);
        }
        return categoryRepository.save(new CategoryDao(UUID.randomUUID().toString(), title));
    }
}
