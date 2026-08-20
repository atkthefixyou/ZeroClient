package com.zeroclient.client.buildai.litematica;

import net.minecraft.world.level.block.state.BlockState;

public class SchematicBlock {
    public final int x, y, z;
    public final BlockState state;

    public SchematicBlock(int x, int y, int z, BlockState state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = state;
    }
}