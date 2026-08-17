package com.zeroclient.client.module.ai;

import com.zeroclient.client.buildai.litematica.AutoAimHelper;
import com.zeroclient.client.buildai.litematica.BlockInteractor;
import com.zeroclient.client.buildai.litematica.BuildPlan;
import com.zeroclient.client.buildai.litematica.BuildScanner;
import com.zeroclient.client.buildai.litematica.LitematicReader;
import com.zeroclient.client.module.ActionConfigEntry;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import com.zeroclient.client.module.TextConfigEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoBuildModule extends Module {

    private BuildPlan plan;
    private int tickCounter = 0;
    private static final int ACTION_INTERVAL_TICKS = 4;

    private String schematicPath = "";

    private final Set<BlockPos> skippedThisSession = new HashSet<>();

    public AutoBuildModule() {
        super("AutoBuild", "Tự động xây theo file .litematic trong tầm với", ModuleCategory.AI_BUILD);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public List<TextConfigEntry> buildTextEntries() {
        return List.of(
                new TextConfigEntry(
                        "Đường dẫn file .litematic",
                        () -> schematicPath,
                        value -> schematicPath = value
                )
        );
    }

    @Override
    public List<ActionConfigEntry> buildActionEntries() {
        return List.of(
                new ActionConfigEntry("Tải Schematic (tại vị trí đứng)", () -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || schematicPath.isBlank()) return;
                    loadSchematic(new File(schematicPath), mc.player.blockPosition());
                })
        );
    }

    public void loadSchematic(File file, BlockPos origin) {
        try {
            this.plan = LitematicReader.read(file, origin);
            skippedThisSession.clear();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Đã tải schematic: " + plan.size() + " block"), false);
            }
        } catch (Exception e) {
            plan = null;
            e.printStackTrace();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
                mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Lỗi đọc schematic: " + detail), false);
            }
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
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "[DEBUG] Skip " + target + " (" + task.desiredBlockId() + ") — không có mặt tựa"), false);
            return;
        }

        int hotbarSlot = BlockInteractor.findHotbarSlot(mc.player, task.desiredBlockId());
        if (hotbarSlot == -1) {
            skippedThisSession.add(target);
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "[DEBUG] Skip " + target + " — hết " + task.desiredBlockId() + " trong hotbar"), false);
            return;
        }

        tickCounter++;
        if (tickCounter < ACTION_INTERVAL_TICKS) return;
        tickCounter = 0;

        if (aimed) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "[DEBUG] Đặt " + task.desiredBlockId() + " tại " + target
                            + " face=" + supportFace + " aimed=" + aimed), false);
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