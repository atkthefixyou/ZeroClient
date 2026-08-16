package com.zeroclient.client.module.render;

import com.zeroclient.client.module.Module;
import com.zeroclient.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;

public class FullbrightModule extends Module {

    private double savedGamma;

    public FullbrightModule() {
        super("Fullbright", "Sáng toàn màn hình, nhìn rõ trong bóng tối", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        var options = Minecraft.getInstance().options;
        savedGamma = options.gamma().get();
        options.gamma().set(16.0);
    }

    @Override
    public void onDisable() {
        Minecraft.getInstance().options.gamma().set(savedGamma);
    }
}
