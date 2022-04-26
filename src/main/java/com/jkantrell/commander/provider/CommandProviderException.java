package com.jkantrell.commander.provider;

import com.jkantrell.commander.exception.CommandArgumentException;

public class CommandProviderException extends RuntimeException {
    public CommandProviderException() {
        super();
    }

    public CommandProviderException(String message) {
        super(message);
    }
}
