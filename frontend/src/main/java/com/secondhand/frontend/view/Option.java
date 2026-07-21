package com.secondhand.frontend.view;

/**
 * A simple (id, name) pair used inside ComboBox items.
 * toString() is what the ComboBox shows to the user.
 */
public class Option {

    public final Long id;
    public final String name;

    public Option(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
