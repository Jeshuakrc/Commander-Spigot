package com.jkantrell.commander.command.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.command.provider.CommandProvider;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;

import java.util.List;

public class BooleanProvider extends CommandProvider<Boolean> {
    private boolean val_;

    @Override
    public List<String> suggest() {
        return List.of("true", "false");
    }
    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        if (!argument.isBool()) {
            throw new CommandArgumentException(argument, "Value must be \"true\" or \"false\".");
        }
        this.val_ = argument.getBool();
        return true;
    }
    @Override
    public Boolean provide() throws CommandException {
        return val_;
    }
}
