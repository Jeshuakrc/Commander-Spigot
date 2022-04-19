package com.jkantrell.commander;

import com.jkantrell.commander.argument.Argument;
import com.jkantrell.commander.argument.ArgumentPipe;
import com.jkantrell.commander.provider.CommandProvider;
import com.jkantrell.commander.provider.builtIn.PlayerProvider;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class Commander {

    //ASSETS
    private class Node {

        private final String label_;
        private final HashMap<String, Commander.Node> children_ = new HashMap<>();
        private final Object commandHolder_;
        private final ArrayList<Method> methods_ = new ArrayList<>();

        private Node(String label, Object holder) {
            this.label_ = label;
            this.commandHolder_ = holder;
        }

        private Commander.Node getNode(String label) {
            if (!this.children_.containsKey(label)) {
                this.children_.put(label,new Node(label,this.commandHolder_));
            }

            return this.children_.get(label);
        }

        private void addMethod(Method method) {
            this.methods_.add(method);
        }

        private boolean run(CommandSender sender, String commandLabel, String[] args) {
            try {
                return this.children_.get(args[0]).run(
                        sender,
                        commandLabel,
                        (args.length > 1) ? Arrays.copyOfRange(args,1,args.length) : new String[0]
                );
            } catch (IndexOutOfBoundsException | NullPointerException ignored) {}

            Method method = this.methods_.get(0);
            ArgumentPipe pipe = new ArgumentPipe();
            for (String s : args) {
                pipe.add(new Argument(s));
            }

            Parameter[] params = method.getParameters();
            Object[] vals = new Object[params.length];
            List<Class<?>> annotations = new ArrayList<>();

            for (int i = 0; i < vals.length; i++) {

                for (Annotation annotation : params[i].getAnnotations()) {
                    annotations.add(annotation.annotationType());
                }

                vals[i] = Commander.this.providers_.get(params[i].getType()).provide(
                        Commander.this,
                        sender,
                        pipe,
                        annotations
                );
            }

            try {
                method.invoke(this.commandHolder_,vals);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    private class TopCommand extends BukkitCommand {

        private final Commander.Node head_;

        protected TopCommand(String name, Object commandHolder) {
            super(name);
            this.head_ = new Commander.Node("",commandHolder);
            Commander.this.commandMap_.register(Commander.this.plugin_.getName(),this);
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return this.head_.run(sender,commandLabel,args);
        }
    }

    //FIELDS
    private final HashMap<Class<?>, CommandProvider<?>> providers_ = new HashMap<>();
    private final Plugin plugin_;
    private final CommandMap commandMap_;
    private final HashMap<String, TopCommand> registrationMap_ = new HashMap<>();

    //CONSTRUCTORS
    public Commander(Plugin mainInstance) {
        this.plugin_ = mainInstance;

        CommandMap commandMap;
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(this.plugin_.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            commandMap = null;
        }
        this.commandMap_ = commandMap;

        this.registerProvider(Player.class, new PlayerProvider());
    }

    //GETTERS
    public Plugin getPlugin() {
        return this.plugin_;
    }

    public void register(CommandHolder commandHolder) {
        for (Method method : commandHolder.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) { continue; }

            String[] labels = StringUtils.split(method.getAnnotation(Command.class).label(),' ');

            TopCommand command = this.registrationMap_.get(labels[0]);
            if (command == null) {
                command = new TopCommand(labels[0], commandHolder);
                this.registrationMap_.put(labels[0],command);
            }

            Commander.Node node = command.head_;
            for (int i = 1; i < labels.length; i++) {
                node = node.getNode(labels[i]);
            }

            node.addMethod(method);

        }
    }

    public <E> void registerProvider(Class<E> clazz, CommandProvider<E> provider) {
        this.providers_.put(clazz,provider);
    }


}
