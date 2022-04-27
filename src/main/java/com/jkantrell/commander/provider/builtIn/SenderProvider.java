package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.provider.CommandProvider;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SenderProvider extends CommandProvider<CommandSender> {

    @Override
    protected void onInitialization() {
        this.setReadyToProvide(true);
    }

    @Override
    public List<String> suggest() {
        return null;
    }

    @Override
    protected boolean handleArgument(Argument argument) {
        return true;
    }

    @Override
    public CommandSender provide() throws CommandException {
        return this.getCommandSender();
    }
}
