package com.zeroclient.client.mixin;

import com.zeroclient.client.module.render.FreecamController;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Redirect(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void onRedirectTurn(LocalPlayer player, double yRot, double xRot) {
        FreecamController controller = FreecamController.instance;

        if (!controller.isActive()) {
            player.turn(yRot, xRot);
            return;
        }

        controller.onPlayerTurn(yRot, xRot);
    }
}