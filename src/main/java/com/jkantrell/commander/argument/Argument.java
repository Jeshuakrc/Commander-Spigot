package com.jkantrell.commander.argument;

import java.util.NoSuchElementException;

public class Argument {

    private final String value_;
    private final Integer intValue_;
    private final Double doubleValue_;
    private final Boolean boolValue_;

    public Argument(String value) {
        this.value_ = value;

        Integer i;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            i = null;
        }
        this.intValue_ = i;

        Double d;
        try {
            d = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            d = null;
        }
        this.doubleValue_ = d;

        Boolean b;
        if (value.equalsIgnoreCase("true")) {
            b = true;
        } else if (value.equalsIgnoreCase("false")) {
            b = false;
        } else if (this.isInt()) {
            b = this.getInt() != 0;
        } else {
            b = null;
        }
        this.boolValue_ = b;

    }

    public String get() {
        return value_;
    }

    public boolean isInt() {
        return this.intValue_ != null;
    }
    public boolean isDouble() {
        return this.doubleValue_ != null;
    }
    public boolean isBool() {
        return this.boolValue_ != null;
    }


    public int getInt() {
        try {
            return this.intValue_.intValue();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("This argument is not an integer");
        }
    }
    public double getDouble() {
        try {
            return this.doubleValue_.doubleValue();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("This argument is not a boolean");
        }
    }
    public boolean getBool() {
        try {
            return this.boolValue_.booleanValue();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("This argument is not a boolean");
        }
    }


}
