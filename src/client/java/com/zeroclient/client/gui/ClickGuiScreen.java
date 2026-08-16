package com.zeroclient.client.gui;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import com.zeroclient.client.module.ModuleManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {

    private static final int COLUMN_WIDTH = 230;
    private static final int COLUMN_GAP = 12;
    private static final int TOP_MARGIN = 70;

    public ClickGuiScreen() {
        super(Component.literal("ZeroClient"));
    }

    @Override
    protected void init() {
        super.init();
        int x = 12;

        for (ModuleCategory category : ModuleCategory.values()) {
            int columnX = x;
            int y = TOP_MARGIN;

            for (Module module : ModuleManager.getInstance().getByCategory(category)) {
                Button button = Button.builder(
                        Component.literal(module.getName() + (module.isEnabled() ? " [ON]" : " [OFF]")),
                        btn -> {
                            module.toggle();
                            btn.setMessage(Component.literal(module.getName() + (module.isEnabled() ? " [ON]" : " [OFF]")));
                        }
                ).bounds(columnX, y, COLUMN_WIDTH - 20, 20).build();

                addRenderableWidget(button);
                y += 24;
            }

            x += COLUMN_WIDTH;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "ZeroClient", 12, 12, 0x4fd1e8, false);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
