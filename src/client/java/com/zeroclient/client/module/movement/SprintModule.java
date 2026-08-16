package com.zeroclient.client.module.movement;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;

public class SprintModule extends Module {

    public SprintModule() {
        super("Sprint", "Tự động chạy nước rút khi di chuyển tới", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        var mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.input != null) {
            boolean movingForward = mc.player.input.keyPresses.forward();
            if (movingForward && !mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        }
    }
}