package com.zeroclient.client.buildai.litematica;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AutoAimHelper {

    private static final float TURN_SPEED = 8.0f;

    public static boolean turnTowards(BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = Vec3.atCenterOf(target);
        Vec3 diff = targetPos.subtract(eyePos);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float desiredYaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(diff.y, dist));

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        float deltaYaw = wrapDegrees(desiredYaw - currentYaw);
        float deltaPitch = desiredPitch - currentPitch;

        float stepYaw = Math.max(-TURN_SPEED, Math.min(TURN_SPEED, deltaYaw));
        float stepPitch = Math.max(-TURN_SPEED, Math.min(TURN_SPEED, deltaPitch));

        player.setYRot(currentYaw + stepYaw);
        player.setXRot(currentPitch + stepPitch);

        return Math.abs(deltaYaw) < 2.0f && Math.abs(deltaPitch) < 2.0f;
    }

    private static float wrapDegrees(float degrees) {
        degrees = degrees % 360.0f;
        if (degrees >= 180.0f) degrees -= 360.0f;
        if (degrees < -180.0f) degrees += 360.0f;
        return degrees;
    }
}