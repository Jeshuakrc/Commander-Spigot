package com.jkantrell.commander.provider;

import com.jkantrell.commander.argument.Argument;
import com.jkantrell.commander.Commander;
import com.jkantrell.commander.argument.ArgumentPipe;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandProvider<E> {

    E provide(Commander commander, CommandSender sender, ArgumentPipe args, List<Class<?>> annotations);

}
