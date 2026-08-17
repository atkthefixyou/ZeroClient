package com.zeroclient.client.buildai.litematica;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LitematicReader {

    public static BuildPlan read(File file, BlockPos placeOrigin) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());

        BuildPlan plan = new BuildPlan();
        plan.setOrigin(placeOrigin);

        CompoundTag regions = root.getCompoundOrEmpty("Regions");
        for (String regionName : regions.keySet()) {
            CompoundTag region = regions.getCompoundOrEmpty(regionName);
            readRegion(region, plan);
        }

        return plan;
    }

    private static void readRegion(CompoundTag region, BuildPlan plan) {
        CompoundTag posTag = region.getCompoundOrEmpty("Position");
        int posX = posTag.getIntOr("x", 0);
        int posY = posTag.getIntOr("y", 0);
        int posZ = posTag.getIntOr("z", 0);

        CompoundTag sizeTag = region.getCompoundOrEmpty("Size");
        int sizeX = sizeTag.getIntOr("x", 0);
        int sizeY = sizeTag.getIntOr("y", 0);
        int sizeZ = sizeTag.getIntOr("z", 0);

        int width = Math.abs(sizeX);
        int height = Math.abs(sizeY);
        int length = Math.abs(sizeZ);

        ListTag paletteTag = region.getListOrEmpty("BlockStatePalette");
        List<String> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            CompoundTag entry = paletteTag.getCompoundOrEmpty(i);
            String blockName = entry.getStringOr("Name", "minecraft:air");
            palette.add(blockName);
        }

        long[] blockStatesArray = region.getLongArray("BlockStates").orElse(new long[0]);

        int totalBlocks = width * height * length;
        if (totalBlocks == 0 || blockStatesArray.length == 0) return;

        int bitsPerBlock = Math.max(2, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));

        net.minecraft.client.Minecraft mcDebug = net.minecraft.client.Minecraft.getInstance();
        if (mcDebug.player != null) {
            mcDebug.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    String.format("[DEBUG] size=%d,%d,%d palette=%d bits=%d longArrLen=%d",
                            sizeX, sizeY, sizeZ, palette.size(), bitsPerBlock, blockStatesArray.length)), false);
        }

        int debugPrinted = 0;

        for (int index = 0; index < totalBlocks; index++) {
            int paletteIndex = readBits(blockStatesArray, index, bitsPerBlock);
            if (paletteIndex <= 0 || paletteIndex >= palette.size()) continue;

            String blockId = palette.get(paletteIndex);
            if ("minecraft:air".equals(blockId)) continue;

            int y = index / (width * length);
            int rem = index % (width * length);
            int z = rem / width;
            int x = rem % width;

            int worldX = posX + (sizeX < 0 ? -x : x);
            int worldY = posY + (sizeY < 0 ? -y : y);
            int worldZ = posZ + (sizeZ < 0 ? -z : z);

            if (debugPrinted < 5 && mcDebug.player != null) {
                mcDebug.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        String.format("[DEBUG] idx=%d local(%d,%d,%d) world(%d,%d,%d) -> %s",
                                index, x, y, z, worldX, worldY, worldZ, blockId)), false);
                debugPrinted++;
            }

            plan.add(new SchematicBlock(worldX, worldY, worldZ, blockId));
        }
    }

    private static int readBits(long[] array, int index, int bitsPerBlock) {
        long bitIndex = (long) index * bitsPerBlock;
        int startArrIndex = (int) (bitIndex / 64L);
        int startBitOffset = (int) (bitIndex % 64L);
        long maxValue = (1L << bitsPerBlock) - 1L;

        if (startArrIndex >= array.length) return 0;

        long value;
        if (startBitOffset + bitsPerBlock > 64 && startArrIndex + 1 < array.length) {
            int bitsInFirst = 64 - startBitOffset;
            value = (array[startArrIndex] >>> startBitOffset)
                    | (array[startArrIndex + 1] << bitsInFirst);
        } else {
            value = array[startArrIndex] >>> startBitOffset;
        }

        return (int) (value & maxValue);
    }
}