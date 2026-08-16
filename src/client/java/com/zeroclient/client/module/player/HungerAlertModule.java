package com.zeroclient.client.module.player;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class HungerAlertModule extends Module {

    private final float threshold = 6.0f;
    private boolean alerted = false;

    public HungerAlertModule() {
        super("HungerAlert", "Cảnh báo khi đói dưới ngưỡng", ModuleCategory.PLAYER);
    }

    @Override
    public void onTick() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float hunger = mc.player.getFoodData().getFoodLevel();

        if (hunger <= threshold && !alerted) {
            mc.player.displayClientMessage(
                    Component.literal("⚠ Đói rồi! Ăn ngay! (" + (int) hunger + "/20)")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            alerted = true;
        } else if (hunger > threshold + 2) {
            alerted = false;
        }
    }
}