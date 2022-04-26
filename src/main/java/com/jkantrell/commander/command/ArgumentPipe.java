package com.jkantrell.commander.command;

import com.jkantrell.commander.exception.NoMoreArgumentsException;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ArgumentPipe extends ArrayList<Argument> {

    private final Commander commander_;
    private final CommanderCommand command_;
    private final CommandSender sender_;

    private final LinkedList<Argument> extracted_ = new LinkedList<>();

    private ArgumentPipe(Commander commander, CommanderCommand command, CommandSender sender) {
        super();
        this.commander_ = commander;
        this.command_ = command;
        this.sender_ = sender;
    }

    private ArgumentPipe(Commander commander, CommanderCommand command, CommandSender sender, Collection<Argument> args) {
        this(commander,command,sender);
        this.addAll(args);
    }

    public ArgumentPipe(Commander commander, CommanderCommand command, CommandSender sender, String[] args) {
        this(commander,command,sender);
        for (String s : args) {
            this.add(new Argument(s));
        }
    }

    public Argument extract() throws NoMoreArgumentsException {
        if (this.extracted_.size() == this.size()) {
            throw new NoMoreArgumentsException("Incomplete command");
        }
        Argument r = this.get(extracted_.size());
        this.extracted_.add(r);
        return r;
    }

    public void restore() {
        this.extracted_.clear();
    }

    public List<Argument> getExtracted() {
        return new ArrayList<>(this.extracted_);
    }

    public List<Argument> getRemaining() {
        ArrayList<Argument> r = new ArrayList<>(this);
        r.removeAll(this.extracted_);
        return r;
    }

    public Argument getLastExtracted() {
        return this.extracted_.getLast();
    }

    public int getExtractedAmount() {
        return this.extracted_.size();
    }

    public int getRemainingAmount() {
        return this.size() - extracted_.size();
    }

    public ArgumentPipe CopyFromRemaining() {
        return new ArgumentPipe(this.commander_,this.command_,this.sender_,this.getRemaining());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Argument arg : this.getRemaining()) {
            builder.append(arg).append(", ");
        }
        builder.setLength(Math.max(builder.length() - 2,1));
        builder.append("]");
        return builder.toString();
    }

}
