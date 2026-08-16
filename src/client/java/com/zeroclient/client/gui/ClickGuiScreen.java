package com.zeroclient.client.gui;

import com.zeroclient.client.gui.theme.GuiTheme;
import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import com.zeroclient.client.module.ModuleManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClickGuiScreen extends Screen {

    public ClickGuiScreen() {
        super(Component.literal("ZeroClient"));
    }

    @Override
    protected void init() {
        super.init();

        int x = GuiTheme.PANEL_GAP;

        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = ModuleManager.getInstance().getByCategory(category);

            int y = GuiTheme.PANEL_TOP + GuiTheme.PANEL_HEADER_HEIGHT + GuiTheme.ROW_PADDING;

            for (Module module : modules) {
                int rowX = x + GuiTheme.ROW_PADDING;
                int rowWidth = GuiTheme.PANEL_WIDTH - GuiTheme.ROW_PADDING * 2;

                addRenderableWidget(new ModuleToggleButton(rowX, y, rowWidth, GuiTheme.ROW_HEIGHT, module));

                y += GuiTheme.ROW_HEIGHT + 2;
            }

            x += GuiTheme.PANEL_WIDTH + GuiTheme.PANEL_GAP;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
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

        super.render(g, mouseX, mouseY, partialTick);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}