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
        log("Bước 1 OK: Litematica class tồn tại");

        try {
            Class<?> dataManagerClass = Class.forName(DATA_MANAGER_CLASS);
            Method getInstance = dataManagerClass.getMethod("getInstance");
            Object dataManager = getInstance.invoke(null);
            log("Bước 2 OK: DataManager.getInstance() = " + dataManager);

            Method getPlacementManager = dataManagerClass.getMethod("getSchematicPlacementManager");
            Object placementManager = getPlacementManager.invoke(dataManager);
            log("Bước 3 OK: placementManager = " + placementManager);

            Method getSelectedPlacement = placementManager.getClass().getMethod("getSelectedSchematicPlacement");
            Object placement = getSelectedPlacement.invoke(placementManager);
            log("Bước 4: getSelectedSchematicPlacement() = " + placement);
            if (placement == null) {
                log("=> placement NULL — Litematica chưa có placement nào đang CHỌN (khác với đã Load)");
                return null;
            }

            Method getSchematic = placement.getClass().getMethod("getSchematic");
            Object schematic = getSchematic.invoke(placement);
            log("Bước 5: schematic = " + schematic);
            if (schematic == null) return null;

            Method getOrigin = placement.getClass().getMethod("getOrigin");
Object mcBlockPosPlacement = getOrigin.invoke(placement);
log("Bước 6: placement.getOrigin() = " + mcBlockPosPlacement);

            BlockPos origin = convertToZeroClientBlockPos(mcBlockPosPlacement);

            BuildPlan plan = new BuildPlan();

            Method getAreaPositions = schematic.getClass().getMethod("getAreaPositions");
            java.util.Map<String, ?> areaPositions = (java.util.Map<String, ?>) getAreaPositions.invoke(schematic);
            log("Bước 7: areaPositions.size() = " + areaPositions.size() + " keys=" + areaPositions.keySet());

            Method getAreaSizes = schematic.getClass().getMethod("getAreaSizes");
            java.util.Map<String, ?> areaSizes = (java.util.Map<String, ?>) getAreaSizes.invoke(schematic);

            Method getSubRegionContainer = schematic.getClass().getMethod("getSubRegionContainer", String.class);

            for (String regionName : areaPositions.keySet()) {
                Object container = getSubRegionContainer.invoke(schematic, regionName);
                log("Region " + regionName + " container=" + container);
                if (container == null) continue;

                Object regionPosObj = areaPositions.get(regionName);
                Object regionSizeObj = areaSizes.get(regionName);
                log("  regionPosObj class=" + (regionPosObj != null ? regionPosObj.getClass().getName() : "null")
                        + " value=" + regionPosObj);
                log("  regionSizeObj class=" + (regionSizeObj != null ? regionSizeObj.getClass().getName() : "null")
                        + " value=" + regionSizeObj);

                BlockPos regionPos = convertToZeroClientBlockPos(regionPosObj);
                BlockPos regionSize = convertToZeroClientBlockPos(regionSizeObj);

                readContainer(container, origin, regionPos, regionSize, plan);
            }

            log("Bước 8: TỔNG block đọc được = " + plan.size());
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

                    String blockId = extractBlockId(blockStateObj);
                    if (blockId == null || "minecraft:air".equals(blockId)) continue;

                    int worldX = origin.getX() + regionPos.getX() + (regionSize.getX() < 0 ? -x : x);
                    int worldY = origin.getY() + regionPos.getY() + (regionSize.getY() < 0 ? -y : y);
                    int worldZ = origin.getZ() + regionPos.getZ() + (regionSize.getZ() < 0 ? -z : z);

                    plan.add(new SchematicBlock(worldX, worldY, worldZ, blockId));
                }
            }
        }
    }

    private static String extractBlockId(Object blockStateObj) throws Exception {
        BlockState state = (BlockState) blockStateObj;
        if (state.isAir()) return "minecraft:air";
        var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null ? key.toString() : null;
    }

    private static BlockPos convertToZeroClientBlockPos(Object mcBlockPos) {
        return (BlockPos) mcBlockPos;
    }
}