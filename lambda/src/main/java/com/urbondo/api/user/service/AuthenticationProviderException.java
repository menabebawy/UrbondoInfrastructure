package com.urbondo.api.user.service;

import com.urbondo.lib.UrbondoException;

public class AuthenticationProviderException extends UrbondoException {
    public AuthenticationProviderException(String message) {
        super(message);
    }
}
