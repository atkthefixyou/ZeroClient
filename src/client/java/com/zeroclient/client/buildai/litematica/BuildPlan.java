package com.zeroclient.client.buildai.litematica;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class BuildPlan {

    private final Map<BlockPos, String> blocks = new HashMap<>();
    private BlockPos origin;

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public void add(SchematicBlock block) {
        if (origin == null) throw new IllegalStateException("Phải setOrigin trước khi add block");
        BlockPos worldPos = origin.offset(block.x, block.y, block.z);
        blocks.put(worldPos, block.blockId);
    }

    public String getDesiredBlockId(BlockPos worldPos) {
        return blocks.get(worldPos);
    }

    public Map<BlockPos, String> getAllBlocks() {
        return blocks;
    }

    public int size() {
        return blocks.size();
    }
}