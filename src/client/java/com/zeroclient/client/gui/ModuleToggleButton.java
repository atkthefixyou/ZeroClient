package com.zeroclient.client.gui;

import com.zeroclient.client.gui.theme.GuiTheme;
import com.zeroclient.client.module.Module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

public class ModuleToggleButton extends Button {

    private final Module module;
    private final Screen parentScreen;

    public ModuleToggleButton(int x, int y, int width, int height, Module module, Screen parentScreen) {
        super(x, y, width, height, Component.literal(module.getName()), btn -> {}, DEFAULT_NARRATION);
        this.module = module;
        this.parentScreen = parentScreen;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo) {
        return mouseButtonInfo.button() == 0 || mouseButtonInfo.button() == 1;
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        boolean isRightClick = mouseButtonEvent.buttonInfo().button() == 1;

        if (module.hasConfig() && isRightClick) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new ModuleConfigScreen(parentScreen, module));
        } else if (!isRightClick) {
            module.toggle();
        }
    }

    @Override
    protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHovered();
        int bg = hovered ? GuiTheme.BG_ROW_HOVER : GuiTheme.BG_ROW;
        g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);

        g.drawString(net.minecraft.client.Minecraft.getInstance().font, module.getName(),
                getX() + 6, getY() + (getHeight() - 8) / 2, GuiTheme.TEXT_NORMAL, false);

        if (module.hasConfig()) {
            g.fill(getX() + getWidth() - GuiTheme.TOGGLE_WIDTH - 12, getY() + getHeight() / 2 - 1,
                    getX() + getWidth() - GuiTheme.TOGGLE_WIDTH - 8, getY() + getHeight() / 2 + 1,
                    GuiTheme.TEXT_DIM);
        }

        int toggleX = getX() + getWidth() - GuiTheme.TOGGLE_WIDTH - 4;
        int toggleY = getY() + (getHeight() - GuiTheme.TOGGLE_HEIGHT) / 2;
        drawToggle(g, toggleX, toggleY, module.isEnabled());
    }

    private void drawToggle(GuiGraphics g, int x, int y, boolean on) {
        int trackColor = on ? GuiTheme.TOGGLE_ON : GuiTheme.TOGGLE_OFF;
        int w = GuiTheme.TOGGLE_WIDTH;
        int h = GuiTheme.TOGGLE_HEIGHT;

        g.fill(x, y, x + w, y + h, trackColor);

        int knobSize = h - 4;
        int knobX = on ? (x + w - knobSize - 2) : (x + 2);
        int knobY = y + 2;
        g.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, GuiTheme.TOGGLE_KNOB);
    }
}