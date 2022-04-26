package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.identify.Sender;
import org.bukkit.entity.Player;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PlayerProvider extends CommandProvider<Player> {

    private Player player_ = null;
    private boolean returnSender = false;

    @Override
    protected void onInitialization() {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(Sender.class)) {
                if (this.getCommandSender() instanceof Player player) {
                    this.player_ = player;
                    this.returnSender = true;
                }
                this.setReadyToProvide(true);
                break;
            }
        }
    }

    @Override
    public List<String> suggest() {
        Collection<? extends Player> players = this.getCommander().getPlugin().getServer().getOnlinePlayers();
        LinkedList<String> r = new LinkedList<>();
        for (Player player : players) {
            r.add(player.getName());
        }
        return r;
    }

    @Override
    public boolean handleArgument(Argument argument) throws CommandException {
        for (Annotation a : this.getAnnotations()) {
            if (a.annotationType().equals(Sender.class)) {
                if (this.getCommandSender() instanceof Player player) {
                    this.player_ = player;
                    return true;
                }
                throw new CommandUnrunnableException("Only a player can run this command");
            }
        }
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
