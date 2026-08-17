package com.zeroclient.client.buildai.litematica;

public class SchematicBlock {
    public final int x, y, z;
    public final String blockId;

    public SchematicBlock(int x, int y, int z, String blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }
}