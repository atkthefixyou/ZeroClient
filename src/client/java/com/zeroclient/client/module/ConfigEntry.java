package com.zeroclient.client.module;

public class ConfigEntry {

    public final String label;
    public final double min;
    public final double max;
    public final java.util.function.DoubleSupplier getter;
    public final java.util.function.DoubleConsumer setter;

    public ConfigEntry(String label, double min, double max,
                        java.util.function.DoubleSupplier getter,
                        java.util.function.DoubleConsumer setter) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }
}