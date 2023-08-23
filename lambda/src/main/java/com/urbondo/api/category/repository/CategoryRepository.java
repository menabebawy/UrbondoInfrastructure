package com.urbondo.api.category.repository;

import com.urbondo.lib.UrbondoRepository;

import java.util.Optional;

public interface CategoryRepository extends UrbondoRepository<CategoryDao> {
    Optional<CategoryDao> findByTitle(String title);
}
