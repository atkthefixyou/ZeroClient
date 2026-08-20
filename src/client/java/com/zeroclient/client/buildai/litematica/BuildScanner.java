package com.zeroclient.client.buildai.litematica;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildScanner {

    public enum TaskType { PLACE, BREAK }

    public record BuildTask(BlockPos pos, TaskType type, BlockState desiredState) {}

    public static List<BuildTask> scan(BuildPlan plan, BlockPos playerPos, int maxRadius) {
        List<BuildTask> tasks = new ArrayList<>();
        var level = Minecraft.getInstance().level;
        if (level == null) return tasks;

        for (Map.Entry<BlockPos, BlockState> entry : plan.getAllBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState desiredState = entry.getValue();

            if (pos.distSqr(playerPos) > (double) maxRadius * maxRadius) continue;

            BlockState currentState = level.getBlockState(pos);

            if (!currentState.equals(desiredState)) {
                if (currentState.isAir()) {
                    tasks.add(new BuildTask(pos, TaskType.PLACE, desiredState));
                } else {
                    tasks.add(new BuildTask(pos, TaskType.BREAK, desiredState));
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
}