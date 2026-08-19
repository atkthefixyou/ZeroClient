package com.zeroclient.client.buildai.pathing;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PathMovementController {

    private List<BlockPos> currentPath = List.of();
    private int currentIndex = 0;
    private boolean active = false;

    public void setPath(List<BlockPos> path) {
        this.currentPath = path;
        this.currentIndex = 0;
        this.active = !path.isEmpty();
    }

    public boolean isActive() {
        return active;
    }

    public void stop() {
        active = false;
        currentPath = List.of();
    }

    public boolean tick() {
        if (!active || currentPath.isEmpty()) return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return true;

        if (currentIndex >= currentPath.size()) {
            active = false;
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

        if (player.input != null) {
            Input old = player.input.keyPresses;
            player.input.keyPresses = new Input(true, false, false, false,
                    old.jump(), old.shift(), old.sprint());
        }

        if (waypoint.getY() > player.blockPosition().getY()) {
            player.jumpFromGround();
        }

        return false;
    }
}