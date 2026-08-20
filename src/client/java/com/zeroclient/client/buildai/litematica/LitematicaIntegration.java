package com.zeroclient.client.buildai.litematica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

public class LitematicaIntegration {

    private static final String DATA_MANAGER_CLASS = "fi.dy.masa.litematica.data.DataManager";

    public static boolean isLitematicaLoaded() {
        try {
            Class.forName(DATA_MANAGER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static BuildPlan readActivePlacement() {
        if (!isLitematicaLoaded()) {
            log("Litematica class KHÔNG tồn tại — chưa cài mod hoặc sai package");
            return null;
        }

        try {
            Class<?> dataManagerClass = Class.forName(DATA_MANAGER_CLASS);
            Method getInstance = dataManagerClass.getMethod("getInstance");
            Object dataManager = getInstance.invoke(null);

            Method getPlacementManager = dataManagerClass.getMethod("getSchematicPlacementManager");
            Object placementManager = getPlacementManager.invoke(dataManager);

            Method getSelectedPlacement = placementManager.getClass().getMethod("getSelectedSchematicPlacement");
            Object placement = getSelectedPlacement.invoke(placementManager);
            if (placement == null) {
                log("=> placement NULL — Litematica chưa có placement nào đang CHỌN");
                return null;
            }

            Method getSchematic = placement.getClass().getMethod("getSchematic");
            Object schematic = getSchematic.invoke(placement);
            if (schematic == null) return null;

            Method getOrigin = placement.getClass().getMethod("getOrigin");
            Object mcBlockPosPlacement = getOrigin.invoke(placement);

            BlockPos origin = convertToZeroClientBlockPos(mcBlockPosPlacement);

            BuildPlan plan = new BuildPlan();

            Method getAreaPositions = schematic.getClass().getMethod("getAreaPositions");
            java.util.Map<String, ?> areaPositions = (java.util.Map<String, ?>) getAreaPositions.invoke(schematic);

            Method getAreaSizes = schematic.getClass().getMethod("getAreaSizes");
            java.util.Map<String, ?> areaSizes = (java.util.Map<String, ?>) getAreaSizes.invoke(schematic);

            Method getSubRegionContainer = schematic.getClass().getMethod("getSubRegionContainer", String.class);

            for (String regionName : areaPositions.keySet()) {
                Object container = getSubRegionContainer.invoke(schematic, regionName);
                if (container == null) continue;

                Object regionPosObj = areaPositions.get(regionName);
                Object regionSizeObj = areaSizes.get(regionName);

                BlockPos regionPos = convertToZeroClientBlockPos(regionPosObj);
                BlockPos regionSize = convertToZeroClientBlockPos(regionSizeObj);

                readContainer(container, origin, regionPos, regionSize, plan);
            }

            log("TỔNG block đọc được = " + plan.size());
            return plan.size() > 0 ? plan : null;

        } catch (Exception e) {
            e.printStackTrace();
            log("LỖI EXCEPTION: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    private static void log(String msg) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("[LTM] " + msg), false);
        }
    }

    private static void readContainer(Object container, BlockPos origin, BlockPos regionPos,
                                       BlockPos regionSize, BuildPlan plan) throws Exception {
        Method get = container.getClass().getMethod("get", int.class, int.class, int.class);

        int width = Math.abs(regionSize.getX());
        int height = Math.abs(regionSize.getY());
        int length = Math.abs(regionSize.getZ());

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    Object blockStateObj = get.invoke(container, x, y, z);
                    if (blockStateObj == null) continue;

                    BlockState state = (BlockState) blockStateObj;
                    if (state.isAir()) continue;

                    int worldX = origin.getX() + regionPos.getX() + (regionSize.getX() < 0 ? -x : x);
                    int worldY = origin.getY() + regionPos.getY() + (regionSize.getY() < 0 ? -y : y);
                    int worldZ = origin.getZ() + regionPos.getZ() + (regionSize.getZ() < 0 ? -z : z);

                    plan.add(new SchematicBlock(worldX, worldY, worldZ, state));
                }
            }
        }
    }

    private static BlockPos convertToZeroClientBlockPos(Object mcBlockPos) {
        return (BlockPos) mcBlockPos;
    }
}