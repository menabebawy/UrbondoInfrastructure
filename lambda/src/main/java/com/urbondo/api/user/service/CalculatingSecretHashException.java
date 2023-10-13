package com.urbondo.api.user.service;

import com.urbondo.lib.UrbondoException;

public class CalculatingSecretHashException extends UrbondoException {
    public CalculatingSecretHashException(String errorMessage) {
        super("Error has occurred while calculating secret hash: " + errorMessage);
    }
}
