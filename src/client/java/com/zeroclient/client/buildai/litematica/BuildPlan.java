package com.zeroclient.client.buildai.litematica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class BuildPlan {

    private final Map<BlockPos, BlockState> blocks = new HashMap<>();

    public void add(SchematicBlock block) {
        BlockPos worldPos = new BlockPos(block.x, block.y, block.z);
        blocks.put(worldPos, block.state);
    }

    public BlockState getDesiredState(BlockPos worldPos) {
        return blocks.get(worldPos);
    }

    public Map<BlockPos, BlockState> getAllBlocks() {
        return blocks;
    }

    public int size() {
        return blocks.size();
    }
}