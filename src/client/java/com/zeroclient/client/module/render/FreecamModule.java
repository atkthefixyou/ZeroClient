package com.zeroclient.client.module.render;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;

public class FreecamModule extends Module {

    public FreecamModule() {
        super("Freecam", "Quan sát tự do, góc nhìn player đóng băng — vẫn đào/tương tác bình thường",
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
}