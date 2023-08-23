package com.urbondo.api.category.service;

import com.urbondo.lib.UrbondoException;

public class CategoryAlreadyExistException extends UrbondoException {
    public CategoryAlreadyExistException(String title) {
        super("category title:" + title + " is already exist.");
    }
}
