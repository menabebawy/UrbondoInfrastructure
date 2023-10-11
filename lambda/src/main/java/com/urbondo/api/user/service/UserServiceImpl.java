package com.urbondo.api.user.service;

import com.urbondo.api.user.repository.UserDao;
import com.urbondo.api.user.repository.UserRepository;
import com.urbondo.lib.ResourceNotFoundException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDao findById(String id) throws ResourceNotFoundException {
        Optional<UserDao> userDAO = userRepository.findById(id);
        if (userDAO.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        return userDAO.get();
    }

    @Override
    public UserDao add(AddUserRequestDto requestDTO) {
        if (userRepository.isEmailExist(requestDTO.email())) {
            throw new UserAlreadyFoundException(requestDTO.email());
        }

        UserDao userDAO = new UserDao(UUID.randomUUID().toString(),
                requestDTO.firstName(),
                requestDTO.lastName(),
                requestDTO.email(),
                requestDTO.phone());

        return userRepository.save(userDAO);
    }


    @Override
    public UserDao update(UpdateUserRequestDto updateUserRequestDTO) {
        UserDao userDAO = findByIdOrThrowException(updateUserRequestDTO.id());

        userDAO.setFirstName(updateUserRequestDTO.firstName());
        userDAO.setLastName(updateUserRequestDTO.lastName());
        userDAO.setPhone(updateUserRequestDTO.phone());

        return userRepository.save(userDAO);
    }

    @Override
    public void deleteBy(String id) {
        userRepository.delete(findByIdOrThrowException(id));
    }

    private UserDao findByIdOrThrowException(String id) {
        Optional<UserDao> userDAO = userRepository.findById(id);
        if (userDAO.isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return userDAO.get();
    }
}
