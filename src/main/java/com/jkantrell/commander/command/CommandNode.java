package com.jkantrell.commander.command;

import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.exception.CommandUnrunnableException;
import com.jkantrell.commander.exception.NoMoreArgumentsException;
import com.jkantrell.commander.provider.CommandProvider;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

class CommandNode {

    //FIELDS
    private final String label_;
    private final Commander commander_;
    private final HashMap<String, CommandNode> children_ = new HashMap<>();
    private final CommandNode parent_;
    private final Object commandHolder_;
    private final ArrayList<Method> methods_ = new ArrayList<>();

    //ASSETS
    private record MethodHolder(Method method, List<CommandProvider<?>> providers, List<Object> vals) {
        Object invoke (Object commandHolder) throws InvocationTargetException, IllegalAccessException {
            this.method().setAccessible(true);
            return this.method().invoke(commandHolder,this.vals.toArray());
        }
    }
    private static class NodeException extends Exception {
        private final CommandException innerException_;
        private int level_;
        private NodeException (CommandException exception, int level) { this.innerException_ = exception; this.level_ = level; }
    }

    //CONSTRUCTORS
    private CommandNode(CommandNode parent, String label, Object holder) {
        this.parent_ = parent;
        this.commander_ = this.parent_.commander_;
        this.label_ = label;
        this.commandHolder_ = holder;
    }

    CommandNode(Commander commander, String label, Object holder) {
        this.parent_ = null;
        this.commander_ = commander;
        this.label_ = label;
        this.commandHolder_ = holder;
    }

    //GETTERS
    CommandNode getNode(String label) {
        if (!this.children_.containsKey(label)) {
            this.children_.put(label,new CommandNode(this,label,this.commandHolder_));
        }

        return this.children_.get(label);
    }
    public String getLabel() {
        return this.label_;
    }


    //PUBLIC METHODS
    void addMethod(Method method) {
        this.methods_.add(method);
    }

    String getFullPath () {
        StringBuilder builder = new StringBuilder();
        CommandNode node = this;
        do {
            builder.append(StringUtils.reverse(node.label_)).append(" ");
            node = node.parent_;
        } while (node != null);
        builder.setLength(builder.length() - 1);
        builder.reverse();

        return builder.toString();
    }

    private List<MethodHolder> getMethods(CommandSender sender, ArgumentPipe args) throws NodeException {
        List<MethodHolder> methods = new ArrayList<>();
        MethodHolder holder;
        for (Method method : this.methods_) {
            holder = new MethodHolder(method,new ArrayList<>(),new ArrayList<>());
            methods.add(holder);
            for (Parameter parameter : method.getParameters()) {
                CommandProvider<?> provider = this.commander_.getProvider(parameter.getType());
                provider.initialize(this.commander_,sender,parameter);
                holder.providers().add(provider);
            }
        }

        LinkedList<NodeException> exceptions = new LinkedList<>();
        Iterator<MethodHolder> iterator;
        Argument arg;
        CommandProvider<?> provider = null;
        MethodHolder method;
        while (true) {
            iterator = methods.iterator();
            while (iterator.hasNext()){
                method = iterator.next();
                Iterator<CommandProvider<?>> providers = method.providers().iterator();
                try {
                    while (providers.hasNext()) {
                        provider = providers.next();
                        if (provider.readyToProvide()) {
                            method.vals().add(provider.provide());
                            providers.remove();
                            continue;
                        }
                        break;
                    }
                } catch (CommandException ex) {
                    exceptions.add(new NodeException(ex, args.getExtractedAmount()));
                    iterator.remove();
                }
            }

            try { arg = args.extract(); } catch (NoMoreArgumentsException ex) { break; }

            iterator = methods.iterator();
            while (iterator.hasNext()){
                method = iterator.next();
                if (method.providers().isEmpty()) {
                    iterator.remove();
                    continue;
                }
                provider = method.providers.get(0);
                try {
                    provider.supply(arg);
                } catch (CommandException ex) {
                    exceptions.add(new NodeException(ex, args.getExtractedAmount()));
                    iterator.remove();
                }
            }
        }

        if (methods.isEmpty() && !exceptions.isEmpty()) {
            exceptions.sort(Comparator.comparingInt(a -> a.level_));
            throw exceptions.getLast();
        }

        return methods;
    }

    List<String> suggest(CommandSender sender, ArgumentPipe args) {
        List<String> r = new LinkedList<>();

        if (args.size() < 1) {
            r.addAll(this.children_.keySet());
        } else if (this.children_.containsKey(args.get(0).getString())) {
            ArgumentPipe subArgs = args.CopyFromRemaining();
            subArgs.remove(0);
            r.addAll(this.children_.get(args.get(0).getString()).suggest(sender,subArgs));
        }

        try {
            for (MethodHolder holder : this.getMethods(sender,args)) {
                if (!holder.providers().isEmpty()) { r.addAll(holder.providers().get(0).suggest()); }
            }

            if (args.size() < 1) {
                r.addAll(this.children_.keySet());
            }
        } catch (NodeException | NullPointerException ignored) {}
        return r;
    }

    boolean run(CommandSender sender, String commandLabel, ArgumentPipe args) throws CommandException {
        try {
            return this.run(sender,commandLabel,args,false);
        } catch (NodeException ignored) { return false; }
    }

    private boolean run(CommandSender sender, String commandLabel, ArgumentPipe args, boolean subCall) throws CommandException, NodeException {
        this.log("Run triggered with args: " + args.toString() + ".");

        NodeException exception = null;
        if (args.size() > 0 && this.children_.containsKey(args.get(0).getString())) {
            String childName = args.get(0).getString();
            this.log("Trying to execute child '" + childName + "'.");
            try {
                ArgumentPipe subArgs = args.CopyFromRemaining();
                subArgs.remove(0);
                return this.children_.get(childName).run(sender, commandLabel, subArgs, true);
            } catch (NodeException ex) {
                ex.level_ = ex.level_ + 1;
                exception = ex;
                this.log("Failed execution of '" + childName + "' due to " + ex.innerException_.getClass().getSimpleName() + ". Level :" + ex.level_ + ".");
            }
        }


        try {
            List<MethodHolder> methods = this.getMethods(sender, args);

            if (methods.isEmpty()) {
                throw new NodeException(new CommandUnrunnableException("Unknown command"),0);
            } else if (methods.size() > 1) {
                methods.sort(Comparator.comparingInt(a -> a.method().getParameterCount()));
                methods.removeIf(a -> a.method().getParameterCount() > methods.get(0).method().getParameterCount());
            }

            Object r = null;
            Iterator<MethodHolder> iterator = methods.iterator();
            MethodHolder method;
            while (iterator.hasNext()) {
                method = iterator.next();
                try {
                    r = method.invoke(this.commandHolder_);
                } catch (IllegalArgumentException e) {
                    iterator.remove();
                    if (!iterator.hasNext()) {
                        throw new NodeException(new NoMoreArgumentsException(""),args.getExtractedAmount());
                    }
                } catch (InvocationTargetException e) {
                    iterator.remove();
                    Throwable innerException = e.getTargetException();

                    CommandException ex;
                    if (innerException instanceof CommandException commandException) {
                        ex = commandException;
                    } else {
                        ex = new CommandUnrunnableException("An error occurred while running command.");
                        this.commander_.getPlugin().getLogger().severe("Unhandled exception while executing command '" + this.getFullPath() + "'.");
                        e.printStackTrace();
                    }
                    if (!iterator.hasNext()) {
                        throw new NodeException(ex,args.getRemainingAmount());
                    }
                } catch (IllegalAccessException e) {
                    iterator.remove();
                    this.commander_.getLogger().severe(
                    "Illegal access to method '" + method.method().getName() + "' from class '" + method.method().getDeclaringClass().getName() + "'."
                    );
                }
            }

            if (methods.size() > 1) {
                this.commander_.getLogger().warning("Ambiguity executing command '" + this.getFullPath() + "'.");
            }

            if (r == null) { return true; }
            if (r instanceof Boolean bool) { return bool; } else { return true; }

        } catch (NodeException ex) {
            this.log("Exception caught. Type: " + ex.innerException_.getClass().getSimpleName() + ", Level: " + ex.level_ + ".");
            if (exception == null || exception.level_ < ex.level_) {
                exception = ex;
            }
        }

        if (subCall) {
            throw exception;
        } else {
            throw exception.innerException_;
        }
    }

    private void log (String msg){
        this.commander_.log(this,msg);
    }
}
