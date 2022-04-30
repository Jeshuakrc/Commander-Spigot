package com.jkantrell.commander.exception;

public class CommandProviderException extends RuntimeException {
    public CommandProviderException() {
        super();
    }

    public CommandProviderException(String message) {
        super(message);
    }
}
