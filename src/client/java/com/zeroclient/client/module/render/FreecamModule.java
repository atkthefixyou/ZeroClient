package com.zeroclient.client.module.render;

import com.zeroclient.client.module.ConfigEntry;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;

import java.util.List;

public class FreecamModule extends Module {

    public FreecamModule() {
        super("Freecam", "Camera bay tự do bằng WASD — góc nhìn player đóng băng, vẫn đào/tương tác bình thường",
                ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        FreecamController.instance.enable();
    }

    @Override
    public void onDisable() {
        FreecamController.instance.disable();
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public List<ConfigEntry> buildConfigEntries() {
        return List.of(
                new ConfigEntry("Tốc độ bay", 0.5, 30.0,
                        () -> FreecamController.instance.getFlySpeed(),
                        FreecamController.instance::setFlySpeed)
        );
    }
}