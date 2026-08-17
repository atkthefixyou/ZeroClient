package com.zeroclient.client.module.ai;

import com.zeroclient.client.buildai.litematica.AutoAimHelper;
import com.zeroclient.client.buildai.litematica.BlockInteractor;
import com.zeroclient.client.buildai.litematica.BuildPlan;
import com.zeroclient.client.buildai.litematica.BuildScanner;
import com.zeroclient.client.buildai.litematica.LitematicReader;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.io.File;
import java.util.List;

public class AutoBuildModule extends Module {

    private BuildPlan plan;
    private int tickCounter = 0;
    private static final int ACTION_INTERVAL_TICKS = 4;

    public AutoBuildModule() {
        super("AutoBuild", "Tự động xây theo file .litematic trong tầm với", ModuleCategory.AI_BUILD);
    }

    public void loadSchematic(File file, BlockPos origin) {
        try {
            this.plan = LitematicReader.read(file, origin);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Đã tải schematic: " + plan.size() + " block"), false);
            }
        } catch (Exception e) {
            plan = null;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Lỗi đọc schematic: " + e.getMessage()), false);
            }
        }
    }

    @Override
    public void onTick() {
        if (plan == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        tickCounter++;
        if (tickCounter < ACTION_INTERVAL_TICKS) return;
        tickCounter = 0;

        BlockPos playerPos = mc.player.blockPosition();
        List<BuildScanner.BuildTask> tasks = BuildScanner.scan(plan, playerPos, 6);
        if (tasks.isEmpty()) return;

        BuildScanner.BuildTask task = tasks.get(0);
        BlockPos target = task.pos();

        if (task.type() == BuildScanner.TaskType.BREAK) {
            boolean aimed = AutoAimHelper.turnTowards(target);
            if (aimed && BlockInteractor.isLookingAt(target)) {
                BlockInteractor.tryBreakBlock(target);
            }
            return;
        }

        Direction supportFace = BlockInteractor.findSupportFace(target);
        if (supportFace == null) return;

        int hotbarSlot = BlockInteractor.findHotbarSlot(mc.player, task.desiredBlockId());
        if (hotbarSlot == -1) return;

        boolean aimed = AutoAimHelper.turnTowards(target);
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