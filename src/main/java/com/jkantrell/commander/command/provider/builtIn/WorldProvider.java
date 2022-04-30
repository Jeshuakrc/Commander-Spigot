package com.jkantrell.commander.command.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.command.provider.CommandProvider;
import org.bukkit.World;
import java.util.List;
import java.util.stream.Collectors;

public class WorldProvider extends CommandProvider<World> {

    private World world_;

    @Override
    public List<String> suggest() {
        return this.getCommander().getPlugin().getServer().getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        this.world_ = this.getCommander().getPlugin().getServer().getWorld(argument.getString());
        if (world_ == null) {
            throw new CommandArgumentException(argument,"No world with name '" + argument.getString() + "' found.");
        }
        return true;
    }

    @Override
    public World provide() throws CommandException {
        return this.world_;
    }
}
