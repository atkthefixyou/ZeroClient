package com.zeroclient.client.module;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextConfigEntry {

    public final String label;
    public final Supplier<String> getter;
    public final Consumer<String> setter;

    public TextConfigEntry(String label, Supplier<String> getter, Consumer<String> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }
}