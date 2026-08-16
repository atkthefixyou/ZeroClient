package com.zeroclient.client.module;

public enum ModuleCategory {
    RENDER("Render"),
    MOVEMENT("Di chuyển"),
    PLAYER("Người chơi"),
    SMP("SMP"),
    AI_BUILD("AI / Xây dựng");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
