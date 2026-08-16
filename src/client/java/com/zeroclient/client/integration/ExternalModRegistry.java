package com.zeroclient.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class ExternalModRegistry {

    public record ExternalMod(String modId, String modName, ConfigScreenFactory<?> factory) {}

    private static List<ExternalMod> cache;

    public static List<ExternalMod> getAvailableMods() {
        if (cache != null) return cache;

        List<ExternalMod> result = new ArrayList<>();

        for (EntrypointContainer<ModMenuApi> entry : FabricLoader.getInstance()
                .getEntrypointContainers("modmenu", ModMenuApi.class)) {
            try {
                ModMenuApi api = entry.getEntrypoint();
                ConfigScreenFactory<?> factory = api.getModConfigScreenFactory();
                if (factory == null) continue;

                ModContainer container = entry.getProvider();
                String modId = container.getMetadata().getId();
                String modName = container.getMetadata().getName();

                if ("zeroclient".equals(modId)) continue;

                result.add(new ExternalMod(modId, modName, factory));
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