package com.zeroclient.client.gui;

import com.zeroclient.client.gui.theme.GuiTheme;
import com.zeroclient.client.module.ActionConfigEntry;
import com.zeroclient.client.module.ConfigEntry;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.TextConfigEntry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModuleConfigScreen extends Screen {

    private final Screen parent;
    private final Module module;

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_TOP = 60;
    private static final int ROW_HEIGHT = 24;

    public ModuleConfigScreen(Screen parent, Module module) {
        super(Component.literal(module.getName() + " Config"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int y = PANEL_TOP + 24;

        List<ConfigEntry> sliders = module.buildConfigEntries();
        for (ConfigEntry entry : sliders) {
            addRenderableWidget(new ConfigSlider(panelX + 10, y, PANEL_WIDTH - 20, 18, entry));
            y += ROW_HEIGHT;
        }

        List<TextConfigEntry> textEntries = module.buildTextEntries();
        for (TextConfigEntry entry : textEntries) {
            EditBox box = new EditBox(this.font, panelX + 10, y + 10, PANEL_WIDTH - 20, 16,
                    Component.literal(entry.label));
            box.setValue(entry.getter.get());
            box.setResponder(entry.setter::accept);
            addRenderableWidget(box);
            y += ROW_HEIGHT + 10;
        }

        List<ActionConfigEntry> actions = module.buildActionEntries();
        for (ActionConfigEntry entry : actions) {
            addRenderableWidget(Button.builder(Component.literal(entry.label), btn -> entry.action.run())
                    .bounds(panelX + 10, y, PANEL_WIDTH - 20, 18).build());
            y += ROW_HEIGHT;
        }

        addRenderableWidget(Button.builder(Component.literal("← Quay lại"), btn -> {
            this.minecraft.setScreen(parent);
        }).bounds(centerX - 40, y + 10, 80, 18).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, GuiTheme.BG_MAIN);

        int centerX = this.width / 2;
        int panelX = centerX - PANEL_WIDTH / 2;

        int sliderCount = module.buildConfigEntries().size();
        int textCount = module.buildTextEntries().size();
        int actionCount = module.buildActionEntries().size();
        int panelHeight = 24
                + sliderCount * ROW_HEIGHT
                + textCount * (ROW_HEIGHT + 10)
                + actionCount * ROW_HEIGHT
                + 34;

        g.fill(panelX, PANEL_TOP, panelX + PANEL_WIDTH, PANEL_TOP + panelHeight, GuiTheme.BG_PANEL);
        g.fill(panelX, PANEL_TOP, panelX + PANEL_WIDTH, PANEL_TOP + 1, GuiTheme.BORDER_CYAN);
        g.fill(panelX, PANEL_TOP, panelX + 1, PANEL_TOP + panelHeight, GuiTheme.BORDER_CYAN);
        g.fill(panelX + PANEL_WIDTH - 1, PANEL_TOP, panelX + PANEL_WIDTH, PANEL_TOP + panelHeight, GuiTheme.BORDER_CYAN);
        g.fill(panelX, PANEL_TOP + panelHeight - 1, panelX + PANEL_WIDTH, PANEL_TOP + panelHeight, GuiTheme.BORDER_CYAN_DIM);

        g.drawCenteredString(this.font, module.getName() + " — Config", centerX, PANEL_TOP + 8, GuiTheme.TEXT_TITLE);

        int y = PANEL_TOP + 24 + sliderCount * ROW_HEIGHT;
        for (TextConfigEntry entry : module.buildTextEntries()) {
            g.drawString(this.font, entry.label, panelX + 10, y, GuiTheme.TEXT_DIM, false);
            y += ROW_HEIGHT + 10;
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ConfigSlider extends AbstractSliderButton {

        private final ConfigEntry entry;

        ConfigSlider(int x, int y, int width, int height, ConfigEntry entry) {
            super(x, y, width, height, Component.literal(""), normalize(entry));
            this.entry = entry;
            updateMessage();
        }

        private static double normalize(ConfigEntry entry) {
            double value = entry.getter.getAsDouble();
            return (value - entry.min) / (entry.max - entry.min);
        }

        @Override
        protected void updateMessage() {
            double value = entry.min + (entry.max - entry.min) * this.value;
            setMessage(Component.literal(entry.label + ": " + String.format("%.1f", value)));
        }

        @Override
        protected void applyValue() {
            double value = entry.min + (entry.max - entry.min) * this.value;
            entry.setter.accept(value);
        }
    }
}