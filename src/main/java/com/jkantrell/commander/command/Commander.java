package com.jkantrell.commander.command;

import com.jkantrell.commander.CommandHolder;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.builtIn.LocationProvider;
import com.jkantrell.commander.provider.builtIn.PlayerProvider;
import com.jkantrell.commander.provider.builtIn.WorldProvider;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Commander {

    //FIELDS
    private final HashMap<Class<?>, Constructor<? extends CommandProvider>> providers_ = new HashMap<>();
    private final JavaPlugin plugin_;
    private final CommandMap commandMap_;
    private final HashMap<String, CommanderCommand> registrationMap_ = new HashMap<>();
    private final Logger logger_;
    private Level logLevel_ = Level.FINEST;

    //CONSTRUCTORS
    public Commander(JavaPlugin mainInstance) {
        this.plugin_ = mainInstance;
        this.logger_ = this.plugin_.getLogger();
        CommandMap commandMap;
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(this.plugin_.getServer());
            this.plugin_.getServer().getPluginManager().registerEvents(new TabListener(this),this.plugin_);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            commandMap = null;
        }
        this.commandMap_ = commandMap;

        this.registerProvider(Player.class, new PlayerProvider());
        this.registerProvider(Location.class, new LocationProvider());
        this.registerProvider(World.class, new WorldProvider());
    }

    //GETTERS
    public JavaPlugin getPlugin() {
        return this.plugin_;
    }
    public Logger getLogger() {
        return this.logger_;
    }
    public Level getLogLevel() {
        return this.logLevel_;
    }
    CommandMap getCommandMap() {
        return this.commandMap_;
    }
    <E> CommandProvider<E> getProvider(Class<E> providerType) {
        try {
            Constructor<? extends CommandProvider> constructor = this.providers_.get(providerType);
            if (constructor == null) {
                return null;
            }
            return (CommandProvider<E>) constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public CommanderCommand getCommand(String label) {
        return this.registrationMap_.get(label.toLowerCase());
    }

    //SETTERS
    public void setLogLevel(Level level) {
        this.logLevel_ = level;
    }

    public void register(CommandHolder commandHolder) {
        for (Method method : commandHolder.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) { continue; }

            String[] labels = StringUtils.split(method.getAnnotation(Command.class).label(),' ');

            CommanderCommand command = this.registrationMap_.get(labels[0]);
            if (command == null) {
                command = new CommanderCommand(this,labels[0], commandHolder);
                this.registrationMap_.put(labels[0].toLowerCase(),command);
            }

            CommandNode node = command.head_;
            for (int i = 1; i < labels.length; i++) {
                node = node.getNode(labels[i]);
            }

            node.addMethod(method);
        }
    }

    public <E> void registerProvider(Class<E> clazz, CommandProvider<E> provider) {
        Class<? extends CommandProvider> providerClass = provider.getClass();
        try {
            this.providers_.put(clazz,providerClass.getConstructor());
        } catch (NoSuchMethodException es) {
            es.printStackTrace();
            throw new IllegalArgumentException("'" + providerClass.getSimpleName() + "' must have a no-parameter constructor to be register to a Commander.");
        }
    }

    void log(CommandNode node, String msg) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(node.getFullPath()).append("] ").append(msg);
        this.logger_.log(this.logLevel_,builder.toString());
    }
}
