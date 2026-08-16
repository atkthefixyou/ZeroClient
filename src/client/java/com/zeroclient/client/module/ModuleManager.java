package com.zeroclient.client.module;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

public class ModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();

    private final Map<String, Module> modulesByName = new LinkedHashMap<>();
    private final Map<ModuleCategory, List<Module>> modulesByCategory = new EnumMap<>(ModuleCategory.class);

    private ModuleManager() {
        for (ModuleCategory cat : ModuleCategory.values()) {
            modulesByCategory.put(cat, new ArrayList<>());
        }
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public void registerAll() {
    register(new com.zeroclient.client.module.movement.SprintModule());
    register(new com.zeroclient.client.module.player.HungerAlertModule());
    register(new com.zeroclient.client.module.smp.DeathLocationModule());
}

    public void register(Module module) {
        modulesByName.put(module.getName().toLowerCase(Locale.ROOT), module);
        modulesByCategory.get(module.getCategory()).add(module);
    }

    public Module get(String name) {
        return modulesByName.get(name.toLowerCase(Locale.ROOT));
    }

    public List<Module> getByCategory(ModuleCategory category) {
        return modulesByCategory.get(category);
    }

    public Collection<Module> getAll() {
        return modulesByName.values();
    }

    public void tickAll() {
        for (Module m : modulesByName.values()) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }
}