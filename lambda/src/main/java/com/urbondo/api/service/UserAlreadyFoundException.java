package com.urbondo.api.service;

import com.urbondo.lib.UrbondoException;

public class UserAlreadyFoundException extends UrbondoException {
    public UserAlreadyFoundException(String email) {
        super("User email:" + email + " is already exist.");
    }
}
