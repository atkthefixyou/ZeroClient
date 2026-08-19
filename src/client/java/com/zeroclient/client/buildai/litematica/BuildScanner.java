package com.zeroclient.client.buildai.litematica;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildScanner {

    public enum TaskType { PLACE, BREAK }

    public record BuildTask(BlockPos pos, TaskType type, String desiredBlockId) {}

    public static List<BuildTask> scan(BuildPlan plan, BlockPos playerPos, int maxRadius) {
        List<BuildTask> tasks = new ArrayList<>();
        var level = Minecraft.getInstance().level;
        if (level == null) return tasks;

        for (Map.Entry<BlockPos, String> entry : plan.getAllBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            String desiredId = entry.getValue();

            if (pos.distSqr(playerPos) > (double) maxRadius * maxRadius) continue;

            BlockState currentState = level.getBlockState(pos);
            String currentId = blockStateToId(currentState);

            if (!desiredId.equals(currentId)) {
                if (currentState.isAir()) {
                    tasks.add(new BuildTask(pos, TaskType.PLACE, desiredId));
                } else {
                    tasks.add(new BuildTask(pos, TaskType.BREAK, desiredId));
                }
            }
        }

        tasks.sort((a, b) -> {
            int layerCompare = Integer.compare(a.pos().getY(), b.pos().getY());
            if (layerCompare != 0) return layerCompare;
            return Double.compare(a.pos().distSqr(playerPos), b.pos().distSqr(playerPos));
        });

        return tasks;
    }

    private static String blockStateToId(BlockState state) {
        if (state.isAir()) return "minecraft:air";
        var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null ? key.toString() : "minecraft:air";
    }
}