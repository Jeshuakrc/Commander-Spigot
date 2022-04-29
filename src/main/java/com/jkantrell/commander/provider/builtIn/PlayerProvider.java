package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.identify.Sender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerProvider extends CommandProvider<Player> {

    private Player player_ = null;
    private boolean returnSender = false;

    @Override
    protected void onInitialization() {
        if (this.isAnnotationPresent(Sender.class)) {
            this.returnSender = true;
            if (this.getCommandSender() instanceof Player player) { this.player_ = player; }
            this.setReadyToProvide(true);
        }
    }

    @Override
    public List<String> suggest() {
        return this.getCommander().getPlugin().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean handleArgument(Argument argument) throws CommandException {
        this.player_ = this.getCommander().getPlugin().getServer().getPlayer(argument.getString());
        if (this.player_ == null) {
            throw new CommandArgumentException(argument,"There's no online player called '" + argument.getString() +"'.");
        }
        return true;
    }

    @Override
    public Player provide() throws CommandException {
        if (player_ == null && returnSender) { throw new CommandUnrunnableException("This command can only be issued by a player."); }
        return this.player_;
    }

}
