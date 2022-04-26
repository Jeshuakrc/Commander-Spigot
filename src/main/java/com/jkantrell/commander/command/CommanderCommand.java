package com.jkantrell.commander.command;

import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import com.jkantrell.commander.exception.NoMoreArgumentsException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.List;

class CommanderCommand extends BukkitCommand {
    protected final CommandNode head_;
    private final Commander commander_;

    CommanderCommand(Commander commander, String name, Object commandHolder) {
        super(name);
        this.commander_ = commander;
        this.head_ = new CommandNode(commander,this.getName(),commandHolder);
        this.commander_.getCommandMap().register(this.commander_.getPlugin().getName(),this);
    }

    void Unregister() {
        this.unregister(this.commander_.getCommandMap());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        ArgumentPipe pipe = new ArgumentPipe(this.commander_,this,sender, args);
        try {
            return this.head_.run(sender, commandLabel, pipe);
        } catch (CommandException e) {
            StringBuilder message = new StringBuilder();
            message.append(ChatColor.RED);

            if (e instanceof CommandArgumentException exception) {
                message.append("Argument Error!").append("\n");
                if (pipe.contains(exception.getArgument())) {
                    message.append(commandLabel).append(" ");
                    for (int i = 0; i <= pipe.indexOf(exception.getArgument()); i++) {
                        message.append(args[i]).append(" ");
                    }
                    message.append("<-- HERE\n");
                }
                message.append(exception.getMessage());
            }
            if (e instanceof NoMoreArgumentsException) {
                message.append("Incomplete command! Usage:\n").append(this.getUsage());
            }
            if (e instanceof CommandUnrunnableException) {
                message.append(e.getMessage());
            }

            sender.sendMessage(message.toString());
            return false;
        }
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        return this.head_.suggest(sender, new ArgumentPipe(this.commander_,this,sender,args));
    }
}
