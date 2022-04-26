package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.provider.CommandProvider;
import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;

public class WorldProvider extends CommandProvider<World> {

    private World world_;

    @Override
    public List<String> suggest() {
        List<World> worlds = this.getCommander().getPlugin().getServer().getWorlds();
        List<String> r = new LinkedList<>();
        for (World world : worlds) { r.add(world.getName()); }
        return r;
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
