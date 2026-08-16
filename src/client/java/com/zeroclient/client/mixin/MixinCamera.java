package com.zeroclient.client.mixin;

import com.zeroclient.client.module.render.FreecamController;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(Level level, Entity entity, boolean detached, boolean mirrored,
                          float partialTicks, CallbackInfo info) {
        FreecamController controller = FreecamController.instance;
        if (controller.isActive()) {
            controller.onRenderTick(partialTicks);
            setRotation(controller.getYRot(), controller.getXRot());
            setPosition(controller.getX(), controller.getY(), controller.getZ());
            info.cancel();
        }
    }
}