package com.jkantrell.commander.command;

import com.jkantrell.commander.CommandHolder;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.builtIn.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
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

        this.registerBuiltInProviders_();
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

    //BEHAVIORAL METHODS
    public void register(CommandHolder commandHolder) {
        List<String> classLabels = Collections.emptyList();
        if (commandHolder.getClass().isAnnotationPresent(Command.class)) {
            classLabels = List.of(StringUtils.split(commandHolder.getClass().getAnnotation(Command.class).label(),' '));
        }

        for (Method method : commandHolder.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) { continue; }

            LinkedList<String> labels = new LinkedList<>(classLabels);
            labels.addAll(List.of(StringUtils.split(method.getAnnotation(Command.class).label(),' ')));

            Iterator<String> i = labels.iterator();
            String label = i.next();
            CommanderCommand command = this.getCommand(label);
            if (command == null) {
                command = new CommanderCommand(this,label, commandHolder);
                this.registrationMap_.put(label.toLowerCase(),command);
            }
            CommandNode node = command.head_;
            while (i.hasNext()) {
                node = node.getNode(i.next());
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

    //PRIVATE METHODS
    private void registerBuiltInProviders_() {
        this.registerProvider(Player.class, new PlayerProvider());
        this.registerProvider(Location.class, new LocationProvider());
        this.registerProvider(World.class, new WorldProvider());
        this.registerProvider(String.class, new StringProvider());
        this.registerProvider(CommandSender.class, new SenderProvider());
    }
}
