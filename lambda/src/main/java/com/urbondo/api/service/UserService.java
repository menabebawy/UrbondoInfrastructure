package com.urbondo.api.service;


import com.urbondo.api.repository.UserDao;
import com.urbondo.lib.ResourceNotFoundException;

public interface UserService {
    UserDao findById(String id) throws ResourceNotFoundException;

    UserDao add(AddUserRequestDto requestDTO);

    UserDao update(UpdateUserRequestDto updateUserRequestDTO);

    void deleteBy(String id);
}