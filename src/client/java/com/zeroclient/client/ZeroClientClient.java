package com.zeroclient.client;

import com.zeroclient.client.gui.ClickGuiScreen;
import com.zeroclient.client.module.ModuleManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public class ZeroClientClient implements ClientModInitializer {

	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		// Đăng ký toàn bộ module (Fullbright, AutoBuild, v.v.)
		ModuleManager.getInstance().registerAll();

		// Keybind mở ClickGui — mặc định Right Shift
		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.zeroclient.open_gui",
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.zeroclient.main"
		));

		// Lắng nghe tick để kiểm tra keybind mở GUI + tick các module đang bật
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new ClickGuiScreen());
				}
			}
			ModuleManager.getInstance().tickAll();
		});
	}
}