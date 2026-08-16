package com.zeroclient.client.module.smp;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class DeathLocationModule extends Module {

    private BlockPos lastDeathPos = null;
    private boolean notified = false;

    public DeathLocationModule() {
        super("DeathLocation", "Ghi lại tọa độ lúc chết, hiển thị lại khi hồi sinh", ModuleCategory.SMP);
    }

    @Override
    public void onTick() {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.player.isDeadOrDying() && lastDeathPos == null) {
            lastDeathPos = mc.player.blockPosition();
        }

        if (lastDeathPos != null && mc.player.isAlive() && !notified) {
            mc.player.displayClientMessage(
                    Component.literal("☠ Bạn đã chết tại: " + lastDeathPos.getX() + ", "
                            + lastDeathPos.getY() + ", " + lastDeathPos.getZ())
                            .withStyle(ChatFormatting.RED),
                    false
            );
            notified = true;
        }

        if (mc.player.isDeadOrDying()) {
            notified = false;
        }
    }
}