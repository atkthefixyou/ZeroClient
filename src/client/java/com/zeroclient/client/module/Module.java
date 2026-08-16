package com.zeroclient.client.module;

public abstract class Module {

    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled = false;

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public boolean hasConfig() {
        return false;
    }

    public java.util.List<ConfigEntry> buildConfigEntries() {
        return java.util.Collections.emptyList();
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }
}