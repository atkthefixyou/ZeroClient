package com.zeroclient.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import com.zeroclient.client.module.ModuleCategory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalModRegistry {

    public record ExternalMod(String modId, String modName, ConfigScreenFactory<?> factory) {}

    private static final Map<String, ModuleCategory> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("freecam", ModuleCategory.RENDER);
        CATEGORY_MAP.put("zoomify", ModuleCategory.RENDER);
        CATEGORY_MAP.put("gammautils", ModuleCategory.RENDER);
        CATEGORY_MAP.put("freelook", ModuleCategory.RENDER);
        CATEGORY_MAP.put("litematica", ModuleCategory.AI_BUILD);
        CATEGORY_MAP.put("appleskin", ModuleCategory.PLAYER);
        CATEGORY_MAP.put("shulkerboxtooltip", ModuleCategory.PLAYER);
    }

    private static Map<ModuleCategory, List<ExternalMod>> cache;

    public static Map<ModuleCategory, List<ExternalMod>> getAvailableModsByCategory() {
        if (cache != null) return cache;

        Map<ModuleCategory, List<ExternalMod>> result = new HashMap<>();
        for (ModuleCategory cat : ModuleCategory.values()) {
            result.put(cat, new ArrayList<>());
        }

        for (EntrypointContainer<ModMenuApi> entry : FabricLoader.getInstance()
                .getEntrypointContainers("modmenu", ModMenuApi.class)) {
            try {
                ModContainer container = entry.getProvider();
                String modId = container.getMetadata().getId();

                ModuleCategory category = CATEGORY_MAP.get(modId);
                if (category == null) continue;
                if ("zeroclient".equals(modId)) continue;

                ModMenuApi api = entry.getEntrypoint();
                ConfigScreenFactory<?> factory = api.getModConfigScreenFactory();
                if (factory == null) continue;

                String modName = container.getMetadata().getName();
                result.get(category).add(new ExternalMod(modId, modName, factory));
            } catch (Throwable t) {
                // Một mod lỗi không được làm sập toàn bộ danh sách
            }
        }

        cache = result;
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Screen createConfigScreen(ExternalMod mod, Screen parent) {
        ConfigScreenFactory<Screen> factory = (ConfigScreenFactory<Screen>) mod.factory();
        return factory.create(parent);
    }
}