package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.argument.Argument;
import com.jkantrell.commander.Commander;
import com.jkantrell.commander.argument.ArgumentPipe;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.identify.Sender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class PlayerProvider implements CommandProvider<Player> {

    @Override
    public Player provide(Commander commander, CommandSender sender, ArgumentPipe args, List<Class<?>> annotations) {
        if (annotations.contains(Sender.class)) {
            return (Player) sender;
        }
        return commander.getPlugin().getServer().getPlayer(args.extract().get());
    }
}
