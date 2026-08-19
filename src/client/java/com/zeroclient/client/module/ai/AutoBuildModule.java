package com.zeroclient.client.module.ai;

import com.zeroclient.client.buildai.litematica.AutoAimHelper;
import com.zeroclient.client.buildai.litematica.BlockInteractor;
import com.zeroclient.client.buildai.litematica.BuildPlan;
import com.zeroclient.client.buildai.litematica.BuildScanner;
import com.zeroclient.client.buildai.litematica.LitematicaIntegration;
import com.zeroclient.client.module.ActionConfigEntry;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoBuildModule extends Module {

    private BuildPlan plan;
    private int tickCounter = 0;
    private static final int ACTION_INTERVAL_TICKS = 4;

    private final Set<BlockPos> skippedThisSession = new HashSet<>();

    public AutoBuildModule() {
        super("AutoBuild", "Tự động xây theo blueprint đang mở trong Litematica", ModuleCategory.AI_BUILD);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public List<ActionConfigEntry> buildActionEntries() {
        return List.of(
                new ActionConfigEntry("Đồng bộ từ Litematica (placement đang chọn)", this::syncFromLitematica)
        );
    }

    private void syncFromLitematica() {
        Minecraft mc = Minecraft.getInstance();

        if (!LitematicaIntegration.isLitematicaLoaded()) {
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.literal("Chưa cài mod Litematica — cần cài để dùng AutoBuild"), false);
            }
            return;
        }

        BuildPlan newPlan = LitematicaIntegration.readActivePlacement();
        if (newPlan == null) {
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.literal("Không tìm thấy placement đang chọn trong Litematica — "
                                + "hãy Load Schematic + chọn Placement trước"), false);
            }
            return;
        }

        this.plan = newPlan;
        skippedThisSession.clear();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("Đã đồng bộ: " + plan.size() + " block từ Litematica"), false);
        }
    }

    @Override
    public void onTick() {
        if (plan == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        List<BuildScanner.BuildTask> tasks = BuildScanner.scan(plan, playerPos, 6);

        BuildScanner.BuildTask task = null;
        for (BuildScanner.BuildTask t : tasks) {
            if (!skippedThisSession.contains(t.pos())) {
                task = t;
                break;
            }
        }

        if (task == null) {
            if (!tasks.isEmpty()) skippedThisSession.clear();
            return;
        }

        BlockPos target = task.pos();
        boolean aimed = AutoAimHelper.turnTowards(target);

        if (task.type() == BuildScanner.TaskType.BREAK) {
            tickCounter++;
            if (tickCounter < ACTION_INTERVAL_TICKS) return;
            tickCounter = 0;

            if (aimed && BlockInteractor.isLookingAt(target)) {
                BlockInteractor.tryBreakBlock(target);
            }
            return;
        }

        Direction supportFace = BlockInteractor.findSupportFace(target);
        if (supportFace == null) {
            skippedThisSession.add(target);
            return;
        }

        int hotbarSlot = BlockInteractor.findHotbarSlot(mc.player, task.desiredBlockId());
        if (hotbarSlot == -1) {
            skippedThisSession.add(target);
            return;
        }

        tickCounter++;
        if (tickCounter < ACTION_INTERVAL_TICKS) return;
        tickCounter = 0;

        if (aimed) {
            BlockInteractor.tryPlaceBlock(target, supportFace, hotbarSlot);
        }
    }

    public boolean hasPlan() {
        return plan != null;
    }

    public int getRemainingTasks(BlockPos playerPos) {
        if (plan == null) return 0;
        return BuildScanner.scan(plan, playerPos, 999).size();
    }
}