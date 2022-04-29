package com.jkantrell.commander.provider;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.command.Commander;
import com.jkantrell.commander.exception.CommandException;
import org.bukkit.command.CommandSender;

import javax.swing.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;

public abstract class CommandProvider<E> {

    private int consecutive_ = 0;
    private Commander commander_ = null;
    private CommandSender commandSender_ = null;
    private Parameter parameter_ = null;
    private boolean isInitialized_ = false;
    private boolean readyToProvide_ = false;

    public final void initialize(Commander commander, CommandSender sender, Parameter parameter) {
        this.consecutive_ = 0;
        this.commander_ = commander;
        this.commandSender_ = sender;
        this.parameter_ = parameter;
        this.isInitialized_ = true;

        this.onInitialization();
    }

    //PROTECTED GETTERS
    protected final Commander getCommander() {
        return this.commander_;
    }
    protected final CommandSender getCommandSender() {
        return this.commandSender_;
    }
    protected final Annotation[] getAnnotations() {
        return this.parameter_.getAnnotations();
    }
    protected final <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationTYpe) {
        return this.parameter_.isAnnotationPresent(annotationTYpe);
    }
    protected final <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return this.parameter_.getAnnotation(annotationType);
    }
    protected final Parameter getParameter() {
        return this.parameter_;
    }
    protected final int getSupplyConsecutive() {
        return this.consecutive_;
    }

    //PUBLIC GETTERS
    public boolean readyToProvide() {
        return this.readyToProvide_;
    }
    public final boolean isInitialized() {
        return this.isInitialized_;
    }

    //PROTECTED SETTERS
    protected void setReadyToProvide(boolean isReadyToProvide) {
        this.readyToProvide_ = isReadyToProvide;
    }

    //PUBLIC METHODS
    public final boolean supply(Argument argument) throws CommandException {
        if (!this.isInitialized_) {
            throw new CommandProviderException("The provider is not yet initialized. Use the 'initialize()' method before supplying.");
        }
        this.consecutive_++;
        this.readyToProvide_ = this.handleArgument(argument);
        return this.readyToProvide_;
    }
    protected void onInitialization() {}

    //ABSTRACT METHODS
    abstract public List<String> suggest();
    abstract protected boolean handleArgument(Argument argument) throws CommandException;
    abstract public E provide() throws CommandException;

}
