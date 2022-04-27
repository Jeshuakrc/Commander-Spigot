package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.provider.CommandProvider;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class StringProvider extends CommandProvider<String> {

    private StringBuilder stringBuilder_ = new StringBuilder();
    private boolean multiArgument_ = false;

    @Override
    public List<String> suggest() {
        return null;
    }

    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        String arg = argument.getString();
        if (arg.substring(1,Math.max(arg.length() - 1,1)).contains("\"")) {
            throw new CommandArgumentException(argument,"Text must not contain quotation marks in between.");
        }

        this.stringBuilder_.append(arg).append(" ");

        if (arg.startsWith("\"")) {
            if (multiArgument_ && arg.length() > 1) {
                throw new CommandArgumentException(argument,"Text must not contain quotation marks in between.");
            }
            this.multiArgument_ = true;
            this.stringBuilder_.deleteCharAt(0);
        }
        if (this.multiArgument_) {
            boolean end = arg.charAt(Math.max(arg.length() - 1, 0)) == '\"' && this.getSupplyConsecutive() > 1;
            if (end) { this.stringBuilder_.setLength(stringBuilder_.length() - 2); }
            return end;
        }
        return true;
    }

    @Override
    public String provide() throws CommandException {
        return StringUtils.normalizeSpace(this.stringBuilder_.toString());
    }
}
