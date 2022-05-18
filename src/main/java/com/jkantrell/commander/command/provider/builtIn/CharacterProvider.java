package com.jkantrell.commander.command.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.command.provider.CommandProvider;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;

import java.util.List;

public class CharacterProvider extends CommandProvider<Character> {
    private char val_;

    @Override
    public List<String> suggest() {
        return List.of("true", "false");
    }
    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        if (argument.getString().length() != 1) {
            throw new CommandArgumentException(argument, "Value must be ONE character.");
        }
        this.val_ = argument.getString().charAt(0);
        return true;
    }
    @Override
    public Character provide() throws CommandException {
        return val_;
    }
}
