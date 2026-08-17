package com.zeroclient.client.module;

public class ActionConfigEntry {

    public final String label;
    public final Runnable action;

    public ActionConfigEntry(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }
}