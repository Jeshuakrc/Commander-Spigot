package com.jkantrell.commander.command.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.command.provider.CommandProvider;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import java.util.Arrays;
import java.util.List;

public class EnumProvider extends CommandProvider<Enum> {

    private Class enumType_;
    private Enum val_;

    @Override
    protected void onInitialization() throws CommandException {
        this.enumType_ = this.getParameter().getType();
    }

    @Override
    public List<String> suggest() {
        return Arrays.stream(enumType_.getEnumConstants()).map(Object::toString).toList();
    }

    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        try {
            this.val_ = Enum.valueOf(this.enumType_, argument.getString());
        } catch (IllegalArgumentException e) {
            throw new CommandArgumentException(argument,"Not a valid argument.");
        }
        return true;
    }

    @Override
    public Enum provide() throws CommandException {
        return this.val_;
    }
}
