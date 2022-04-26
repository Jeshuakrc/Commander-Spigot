package com.jkantrell.commander.exception;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.command.ArgumentPipe;

abstract public class CommandException extends Exception {

    public CommandException(String message) {
        super(message);
    }


}
