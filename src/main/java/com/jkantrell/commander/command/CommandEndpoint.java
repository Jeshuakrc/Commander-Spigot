package com.jkantrell.commander.command;

import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import com.jkantrell.commander.exception.NoMoreArgumentsException;
import com.jkantrell.commander.command.provider.CommandProvider;
import org.bukkit.command.CommandSender;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CommandEndpoint {

    //FIELDS
    private final CommandNode parent_;
    private final Commander commander_;
    private final Method method_;
    private final LinkedList<CommandProvider<?>> providers_ = new LinkedList<>();
    private int pointer_ = 0;
    private boolean readyToRun_ = false;

    //CONSTRUCTOR
    CommandEndpoint(CommandNode parent, Method method) {
        this.parent_ = parent;
        this.method_ = method;
        this.commander_ = this.parent_.getCommander();
    }

    //GETTERS
    boolean isReadyToRun() {
        return this.readyToRun_;
    }
    int getParameterCount() {
        return this.method_.getParameterCount();
    }
    int consumedArguments() {
        return this.pointer_;
    }

    //METHODS
    void initialize(CommandSender sender) {
        this.pointer_ = 0;
        this.providers_.clear();
        for (Parameter parameter : this.method_.getParameters()) {
            CommandProvider<?> provider = this.commander_.getProvider(parameter.getType());
            this.providers_.add(provider);
            provider.initialize(this.commander_,sender,parameter,new ArrayList<>(this.providers_));
        }
        if (this.providers_.isEmpty()) { this.readyToRun_ = true; return; }
        this.readyToRun_ = this.providers_.stream().allMatch(CommandProvider::readyToProvide);
    }

    boolean supplyArgument(Argument arg) throws CommandException {
        CommandProvider<?> provider;
        boolean supplied = false;
        while (true) {
            if (this.pointer_ >= this.providers_.size()) {
                this.readyToRun_ = true;
                return true;
            }
            provider = this.providers_.get(this.pointer_);
            if (provider.readyToProvide()) {
                this.pointer_++;
                continue;
            }
            if (supplied) { break; }
            try {
                provider.supply(arg);
            } catch (CommandException e) {
                this.readyToRun_ = true;
                throw e;
            }

            supplied = true;
        }
        return false;
    }

    List<String> suggest() {
        CommandProvider<?> provider = this.providers_.stream()
                .filter(p -> !p.readyToProvide())
                .findFirst()
                .orElse(null);

        return (provider == null) ? Collections.emptyList() : provider.suggest();
    }

    Object run() throws CommandException {
        LinkedList<Object> vals = new LinkedList<>();
        for (CommandProvider<?> provider : this.providers_) {
            if (provider.readyToProvide()) {
                vals.add(provider.provide());
            } else { break; }
        }
        try {
            return this.method_.invoke(this.parent_.getCommandHolder(), vals.toArray());
        } catch (IllegalArgumentException e) {
            throw new NoMoreArgumentsException("");
        } catch (InvocationTargetException e) {
            Throwable innerException = e.getTargetException();
            CommandException ex;
            if (innerException instanceof CommandException commandException) {
                ex = commandException;
            } else {
                ex = new CommandUnrunnableException("An error occurred while running command.");
                this.commander_.getPlugin().getLogger().severe("Unhandled exception while executing command '" + this.parent_.getFullPath() + "'.");
                e.printStackTrace();
            }
            throw ex;
        } catch (IllegalAccessException e) {
            this.commander_.getLogger().severe(
                    "Illegal access to method '" + this.method_.getName() + "' from class '" + this.method_.getDeclaringClass().getName() + "'."
            );
            throw new CommandUnrunnableException("An error occurred while running command.");
        }
    }
}
