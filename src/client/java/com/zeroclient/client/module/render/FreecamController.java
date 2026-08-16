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
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);

    private boolean active;
    private CameraType oldCameraType;
    private ClientInput playerInput;
    private ClientInput freecamInput;

    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity, leftVelocity, upVelocity;
    private long lastTime;

    private double flySpeed = 10.0;

    private FreecamController() {}

    public boolean isActive() {
        return active;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getXRot() { return xRot; }
    public float getYRot() { return yRot; }

    public double getFlySpeed() { return flySpeed; }
    public void setFlySpeed(double speed) { this.flySpeed = Math.max(0.5, speed); }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (active) return;

        Entity entity = mc.getCameraEntity();
        if (mc.player == null || entity == null) return;

        active = true;
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

        forwardVelocity = 0;
        leftVelocity = 0;
        upVelocity = 0;
        lastTime = 0;
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
        if (!active) return false;

        this.yRot += (float) deltaYRot * 0.15F;
        this.xRot += (float) deltaXRot * 0.15F;
        this.xRot = Mth.clamp(this.xRot, -90, 90);
        calculateVectors();
        return true;
    }

    public void onRenderTick(float partialTicks) {
        if (!active || playerInput == null) return;

        long currTime = System.nanoTime();
        double frameTime = lastTime == 0 ? 0 : (currTime - lastTime) / 1_000_000_000.0;
        lastTime = currTime;
        if (frameTime <= 0 || frameTime > 1) return;

        var keys = playerInput.keyPresses;
        double forwardImpulse = calcImpulse(keys.forward(), keys.backward());
        double leftImpulse = calcImpulse(keys.left(), keys.right());
        double upImpulse = calcImpulse(keys.jump(), keys.shift());

        double acceleration = flySpeed * 4;
        double slowdown = 0.85;

        forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, acceleration, slowdown);
        leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, acceleration, slowdown);
        upVelocity = combineMovement(upVelocity, upImpulse, frameTime, acceleration, slowdown);

        double dx = (forwards.x() * forwardVelocity + left.x() * leftVelocity + up.x() * upVelocity) * frameTime;
        double dy = (forwards.y() * forwardVelocity + left.y() * leftVelocity + up.y() * upVelocity) * frameTime;
        double dz = (forwards.z() * forwardVelocity + left.z() * leftVelocity + up.z() * upVelocity) * frameTime;

        x += dx;
        y += dy;
        z += dz;
    }

    private double calcImpulse(boolean positive, boolean negative) {
        if (positive == negative) return 0.0;
        return positive ? 1.0 : -1.0;
    }

    private double combineMovement(double velocity, double impulse, double frameTime, double acceleration, double slowdown) {
        if (impulse != 0) {
            if (impulse > 0 && velocity < 0) velocity = 0;
            if (impulse < 0 && velocity > 0) velocity = 0;
            velocity += acceleration * impulse * frameTime;
        } else {
            velocity *= slowdown;
        }
        return velocity;
    }

    private void calculateVectors() {
        rotation.rotationYXZ(-yRot * ((float) Math.PI / 180F), xRot * ((float) Math.PI / 180F), 0.0F);
        forwards.set(0.0F, 0.0F, 1.0F).rotate(rotation);
        up.set(0.0F, 1.0F, 0.0F).rotate(rotation);
        left.set(1.0F, 0.0F, 0.0F).rotate(rotation);
    }
}