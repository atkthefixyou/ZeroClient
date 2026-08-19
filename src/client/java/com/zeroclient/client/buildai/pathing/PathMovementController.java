package com.zeroclient.client.buildai.pathing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PathMovementController {

    private static class PathInput extends ClientInput {
        boolean forward = false;
        boolean jump = false;

        @Override
        public void tick() {
            this.keyPresses = new Input(forward, false, false, false, jump, false, false);
            this.moveVector = new Vec2(0.0f, forward ? 1.0f : 0.0f);
        }
    }

    private List<BlockPos> currentPath = List.of();
    private int currentIndex = 0;
    private boolean active = false;

    private ClientInput originalInput;
    private PathInput pathInput;

    public void setPath(List<BlockPos> path) {
        this.currentPath = path;
        this.currentIndex = 0;
        this.active = !path.isEmpty();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.input != pathInput) {
            originalInput = mc.player.input;
            pathInput = new PathInput();
            mc.player.input = pathInput;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void stop() {
        active = false;
        currentPath = List.of();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && originalInput != null) {
            mc.player.input = originalInput;
            originalInput = null;
        }
    }

    public boolean tick() {
        if (!active || currentPath.isEmpty()) return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return true;

        if (currentIndex >= currentPath.size()) {
            stop();
            return true;
        }

        BlockPos waypoint = currentPath.get(currentIndex);
        Vec3 playerPos = player.position();
        Vec3 waypointCenter = Vec3.atBottomCenterOf(waypoint);

        double dx = waypointCenter.x - playerPos.x;
        double dz = waypointCenter.z - playerPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < 0.3) {
            currentIndex++;
            return false;
        }

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        player.setYRot(targetYaw);

        if (pathInput != null) {
            pathInput.forward = true;
            pathInput.jump = waypoint.getY() > player.blockPosition().getY();
        }

        return false;
    }
}