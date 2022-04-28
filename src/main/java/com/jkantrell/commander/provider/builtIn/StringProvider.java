package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.provider.CommandProvider;

import java.util.List;

public class StringProvider extends CommandProvider<String> {

    private String string_ = null;

    @Override
    public List<String> suggest() {
        return null;
    }

    @Override
    protected boolean handleArgument(Argument argument) {
        this.string_ = argument.getString();
        return true;
    }

    @Override
    public String provide() throws CommandException {
        return this.string_;
    }
}
