package com.urbondo.api.user.service;

import com.urbondo.lib.UrbondoException;

public class UserNotFoundException extends UrbondoException {
    public UserNotFoundException(String id) {
        super("User id:" + id + " is not found.");
    }
}
