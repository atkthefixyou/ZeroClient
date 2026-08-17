package com.zeroclient.client.buildai.litematica;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockInteractor {

    public static int findHotbarSlot(Player player, String blockId) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BlockItem blockItem) {
                var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
                if (key != null && key.toString().equals(blockId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static Direction findSupportFace(BlockPos target) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.relative(dir);
            if (!level.getBlockState(neighbor).isAir()) {
                return dir.getOpposite();
            }
        }
        return null;
    }

    public static boolean tryPlaceBlock(BlockPos target, Direction supportFace, int hotbarSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return false;

        mc.player.getInventory().setSelectedSlot(hotbarSlot);

        BlockPos against = target.relative(supportFace.getOpposite());
        Vec3 hitVec = Vec3.atCenterOf(against).relative(supportFace.getOpposite(), 0.5);

        BlockHitResult hitResult = new BlockHitResult(hitVec, supportFace, against, false);

        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, hitResult);
        return true;
    }

    public static boolean tryBreakBlock(BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return false;

        mc.gameMode.destroyBlock(target);
        return true;
    }

    public static boolean isLookingAt(BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null) return false;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return false;
        BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
        return blockHit.getBlockPos().equals(target);
    }
}