package com.urbondo.api.user.service;

import com.google.gson.JsonObject;
import com.urbondo.api.user.repository.UserDao;
import com.urbondo.api.user.repository.UserRepository;
import com.urbondo.api.user.service.dto.SignupRequestDto;
import com.urbondo.api.user.service.dto.UpdateUserRequestDto;
import com.urbondo.lib.ResourceNotFoundException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import javax.inject.Inject;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CognitoService cognitoService;

    @Inject
    public UserServiceImpl(UserRepository userRepository, CognitoService cognitoService) {
        this.userRepository = userRepository;
        this.cognitoService = cognitoService;
    }


    @Override
    public JsonObject signup(SignupRequestDto signupRequestDto) {
        try {
            return cognitoService.signup(signupRequestDto);
        } catch (AwsServiceException exception) {
            throw new AuthenticationProviderException(exception.getLocalizedMessage());
        }
    }

    @Override
    public JsonObject confirmSignUp(String code, String username) {
        try {
            return cognitoService.confirmSignUp(code, username);
        } catch (AwsServiceException exception) {
            throw new AuthenticationProviderException(exception.getLocalizedMessage());
        }
    }

    @Override
    public JsonObject resendConfirmationCode(String userName) {
        try {
            return cognitoService.resendConfirmationCode(userName);
        } catch (AwsServiceException exception) {
            throw new AuthenticationProviderException(exception.getLocalizedMessage());
        }
    }

    @Override
    public AuthenticationResultType login(String username, String password) {
        try {
            AuthenticationResultType authenticationResultType = cognitoService.login(username, password);
            UserDao cognitoUser = cognitoService.getUser(authenticationResultType.accessToken());

            userRepository.save(new UserDao(cognitoUser.getId(),
                                            cognitoUser.getFirstName(),
                                            cognitoUser.getLastName(),
                                            cognitoUser.getEmail(),
                                            cognitoUser.getPhone()));

            return authenticationResultType;
        } catch (AuthenticationProviderException exception) {
            throw new AuthenticationProviderException(exception.getLocalizedMessage());
        }
    }

    @Override
    public AuthenticationResultType refreshToken(String refreshToken, String username) {
        return cognitoService.refreshToken(refreshToken, username);
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
