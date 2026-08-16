package com.zeroclient.client.gui;

import com.zeroclient.client.gui.theme.GuiTheme;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import com.zeroclient.client.module.ModuleManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private static class ModuleRow {
        final Module module;
        int x, y, width, height;

        ModuleRow(Module module) {
            this.module = module;
        }

        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private final List<ModuleRow> rows = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("ZeroClient"));
    }

    @Override
    protected void init() {
        super.init();
        rows.clear();

        int x = GuiTheme.PANEL_GAP;

        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = ModuleManager.getInstance().getByCategory(category);

            int y = GuiTheme.PANEL_TOP + GuiTheme.PANEL_HEADER_HEIGHT + GuiTheme.ROW_PADDING;

            for (Module module : modules) {
                ModuleRow row = new ModuleRow(module);
                row.x = x + GuiTheme.ROW_PADDING;
                row.y = y;
                row.width = GuiTheme.PANEL_WIDTH - GuiTheme.ROW_PADDING * 2;
                row.height = GuiTheme.ROW_HEIGHT;
                rows.add(row);

                y += GuiTheme.ROW_HEIGHT + 2;
            }

            x += GuiTheme.PANEL_WIDTH + GuiTheme.PANEL_GAP;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        g.fill(0, 0, this.width, this.height, GuiTheme.BG_MAIN);

        g.drawString(this.font, "ZeroClient", GuiTheme.PANEL_GAP, 14, GuiTheme.TEXT_TITLE, false);
        g.drawString(this.font, "Right Shift để đóng", GuiTheme.PANEL_GAP, 26, GuiTheme.TEXT_DIM, false);

        int x = GuiTheme.PANEL_GAP;
        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = ModuleManager.getInstance().getByCategory(category);
            int panelHeight = GuiTheme.PANEL_HEADER_HEIGHT
                    + modules.size() * (GuiTheme.ROW_HEIGHT + 2)
                    + GuiTheme.ROW_PADDING;

            drawPanel(g, x, GuiTheme.PANEL_TOP, GuiTheme.PANEL_WIDTH, panelHeight, category.getDisplayName());

            x += GuiTheme.PANEL_WIDTH + GuiTheme.PANEL_GAP;
        }

        for (ModuleRow row : rows) {
            boolean hovered = row.isHovered(mouseX, mouseY);
            drawModuleRow(g, row, hovered);
        }
    }

    private void drawPanel(GuiGraphics g, int x, int y, int width, int height, String title) {
        g.fill(x, y, x + width, y + height, GuiTheme.BG_PANEL);

        g.fill(x, y, x + width, y + 1, GuiTheme.BORDER_CYAN);
        g.fill(x, y + height - 1, x + width, y + height, GuiTheme.BORDER_CYAN_DIM);
        g.fill(x, y, x + 1, y + height, GuiTheme.BORDER_CYAN);
        g.fill(x + width - 1, y, x + width, y + height, GuiTheme.BORDER_CYAN);

        g.fill(x + 1, y + 1, x + width - 1, y + GuiTheme.PANEL_HEADER_HEIGHT, 0xFF15304F);
        g.drawCenteredString(this.font, title, x + width / 2, y + 9, GuiTheme.TEXT_TITLE);
    }

    private void drawModuleRow(GuiGraphics g, ModuleRow row, boolean hovered) {
        int bg = hovered ? GuiTheme.BG_ROW_HOVER : GuiTheme.BG_ROW;
        g.fill(row.x, row.y, row.x + row.width, row.y + row.height, bg);

        g.drawString(this.font, row.module.getName(), row.x + 6, row.y + (row.height - 8) / 2,
                GuiTheme.TEXT_NORMAL, false);

        int toggleX = row.x + row.width - GuiTheme.TOGGLE_WIDTH - 4;
        int toggleY = row.y + (row.height - GuiTheme.TOGGLE_HEIGHT) / 2;
        drawToggle(g, toggleX, toggleY, row.module.isEnabled());
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (ModuleRow row : rows) {
                if (row.isHovered((int) mouseX, (int) mouseY)) {
                    row.module.toggle();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}