package com.urbondo.api.category.service;


import com.urbondo.api.category.repository.CategoryDao;

public interface CategoryService {
    CategoryDao findById(String id);

    CategoryDao add(String title);
}
