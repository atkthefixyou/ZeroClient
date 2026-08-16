package com.zeroclient.client;

import com.zeroclient.client.gui.ClickGuiScreen;
import com.zeroclient.client.module.ModuleManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class ZeroClientClient implements ClientModInitializer {

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("zeroclient", "main"));

	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		ModuleManager.getInstance().registerAll();

		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.zeroclient.open_gui",
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				CATEGORY
		));

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