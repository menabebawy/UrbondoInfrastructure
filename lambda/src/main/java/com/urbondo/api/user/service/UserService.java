package com.urbondo.api.user.service;


import com.urbondo.api.user.repository.UserDao;
import com.urbondo.lib.ResourceNotFoundException;

public interface UserService {
    UserDao findById(String id) throws ResourceNotFoundException;

    UserDao add(AddUserRequestDto requestDTO);

    UserDao update(UpdateUserRequestDto updateUserRequestDTO);

    void deleteBy(String id);
}