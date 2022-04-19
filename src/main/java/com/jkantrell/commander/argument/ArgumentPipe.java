package com.jkantrell.commander.argument;

import java.util.LinkedList;

public class ArgumentPipe extends LinkedList<Argument> {

    public Argument extract() {
        Argument r = this.get(0);
        this.remove(0);
        return r;
    }

}
