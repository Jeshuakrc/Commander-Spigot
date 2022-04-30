package com.jkantrell.commander.exception;

import com.jkantrell.commander.command.Argument;

public class CommandArgumentException extends CommandException {

    private final Argument argument_;

    public CommandArgumentException(Argument argument, String message) {
        super(message);
        this.argument_ = argument;
    }

    public Argument getArgument() {
        return this.argument_;
    }

}
