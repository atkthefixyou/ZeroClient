package com.zeroclient.client.gui;

import com.zeroclient.client.gui.theme.GuiTheme;
import com.zeroclient.client.integration.ExternalModRegistry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ExternalModButton extends Button {

    private final ExternalModRegistry.ExternalMod mod;

    public ExternalModButton(int x, int y, int width, int height,
                              ExternalModRegistry.ExternalMod mod, Screen parentScreen) {
        super(x, y, width, height, Component.literal(mod.modName()), btn -> {
            Screen configScreen = ExternalModRegistry.createConfigScreen(mod, parentScreen);
            net.minecraft.client.Minecraft.getInstance().setScreen(configScreen);
        }, DEFAULT_NARRATION);
        this.mod = mod;
    }

    @Override
    protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHovered();
        int bg = hovered ? GuiTheme.BG_ROW_HOVER : GuiTheme.BG_ROW;
        g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);

        g.drawString(net.minecraft.client.Minecraft.getInstance().font, mod.modName(),
                getX() + 6, getY() + (getHeight() - 8) / 2, GuiTheme.TEXT_NORMAL, false);
    }
}