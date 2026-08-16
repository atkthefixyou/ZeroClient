package com.zeroclient.client.module;

import com.zeroclient.client.module.render.FullbrightModule;

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

    /** Đăng ký toàn bộ module ở đây — thêm dòng mới mỗi khi tạo module mới */
    public void registerAll() {
        register(new FullbrightModule());
        // register(new ZoomModule());
        // register(new AutoBuildModule());
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

    /** Gọi mỗi tick từ event bus — chỉ tick module đang bật */
    public void tickAll() {
        for (Module m : modulesByName.values()) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }
}
