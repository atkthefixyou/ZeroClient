package com.zeroclient.client.module.render;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamController {

    public static final FreecamController instance = new FreecamController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);

    private boolean active;
    private boolean eyeLock;
    private CameraType oldCameraType;
    private ClientInput playerInput;
    private ClientInput freecamInput;

    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity, leftVelocity, upVelocity;

    private FreecamController() {}

    public boolean isActive() {
        return active;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getXRot() { return xRot; }
    public float getYRot() { return yRot; }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void toggleEyeLock() {
        if (active) {
            eyeLock = !eyeLock;
        }
    }

    public boolean isEyeLock() {
        return eyeLock;
    }

    public void enable() {
        if (active) return;

        Entity entity = mc.getCameraEntity();
        if (mc.player == null || entity == null) return;

        active = true;
        eyeLock = true;
        oldCameraType = mc.options.getCameraType();
        playerInput = mc.player.input;
        mc.player.input = freecamInput = new ClientInput();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);

        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 pos = entity.getEyePosition(partialTicks);
        x = pos.x;
        y = pos.y;
        z = pos.z;
        yRot = entity.getViewYRot(partialTicks);
        xRot = entity.getViewXRot(partialTicks);

        calculateVectors();

        double distance = -2;
        x += forwards.x() * distance;
        y += forwards.y() * distance;
        z += forwards.z() * distance;

        forwardVelocity = 0;
        leftVelocity = 0;
        upVelocity = 0;
    }

    public void disable() {
        if (!active) return;
        if (mc.player == null) return;

        active = false;
        mc.options.setCameraType(oldCameraType);
        mc.player.input = playerInput;
        oldCameraType = null;
    }

    public void onClientTickStart() {
        if (active && mc.player != null && mc.player.input != playerInput) {
            playerInput.tick();
        }
    }

    public boolean onPlayerTurn(double deltaYRot, double deltaXRot) {
        if (active && !eyeLock) {
            this.xRot += (float) deltaXRot * 0.15F;
            this.yRot += (float) deltaYRot * 0.15F;
            this.xRot = Mth.clamp(this.xRot, -90, 90);
            calculateVectors();
            return true;
        }
        return !active;
    }

    public void applyEyeLock(float partialTicks) {
        if (!active || !eyeLock) return;

        Entity entity = mc.getCameraEntity();
        if (entity == null) return;

        Vec3 pos = entity.getEyePosition(partialTicks);
        double dx = x - pos.x;
        double dy = y - pos.y;
        double dz = z - pos.z;
        this.xRot = (float) (Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180);
        this.yRot = (float) (Math.atan2(dz, dx) / Math.PI * 180 + 90);
        this.xRot = Mth.clamp(this.xRot, -90, 90);
        calculateVectors();
    }

    private void calculateVectors() {
        rotation.rotationYXZ(-yRot * ((float) Math.PI / 180F), xRot * ((float) Math.PI / 180F), 0.0F);
        forwards.set(0.0F, 0.0F, 1.0F).rotate(rotation);
    }
}