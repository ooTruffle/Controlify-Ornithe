/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.legacy;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.List;

/** Backport of upstream's hold-and-release radial selection and focus timeout. */
public final class RadialMenuScreen extends Screen {
	private static final Identifier TEXTURE = new Identifier("controlify", "textures/gui/radial-buttons.png");
	private static final int FOCUS_TIMEOUT = 20;
	private static final int BUTTON_SIZE = 32;
	private static final List<List<Action>> PAGES = List.of(
			List.of(Action.HUD, Action.PERSPECTIVE, Action.DROP_STACK, Action.CHAT,
					Action.PICK_BLOCK, Action.SCREENSHOT, Action.PLAYER_LIST, Action.DEBUG),
			List.of(Action.PROFILER, Action.TPS, Action.RELOAD_CHUNKS, Action.TOOLTIPS,
					Action.RELOAD_RESOURCES, Action.CLEAR_CHAT));

	private int pageIndex;
	private int selectedButton = -1;
	private int idleTicks;
	private int age;

	void onControllerInput(boolean held, boolean cancel, int pageDirection, float x, float y) {
		if (cancel) {
			minecraft.openScreen(null);
			return;
		}
		if (!held) {
			Action action = selectedButton < 0 ? null : page().get(selectedButton);
			minecraft.openScreen(null);
			if (action != null) ControllerManager.invokeRadialAction(minecraft, action);
			return;
		}
		if (pageDirection != 0) {
			pageIndex = Math.floorMod(pageIndex + pageDirection, PAGES.size());
			selectedButton = -1;
			age = 0;
		}
		float threshold = LegacyConfig.activationThreshold();
		if (Math.abs(x) >= threshold || Math.abs(y) >= threshold) {
			double angle = Math.atan2(y, x) + Math.PI / 2;
			double each = 2 * Math.PI / page().size();
			selectedButton = Math.floorMod((int) Math.floor((angle + each / 2) / each), page().size());
			idleTicks = 0;
		} else if (++idleTicks >= FOCUS_TIMEOUT) {
			selectedButton = -1;
		}
	}

	private List<Action> page() {
		return PAGES.get(pageIndex);
	}

	@Override
	public void tick() {
		age = Math.min(5, age + 1);
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		int centerX = width / 2;
		int centerY = height / 2;
		List<Action> items = page();
		double radius = Math.max((Math.hypot(BUTTON_SIZE, BUTTON_SIZE) + 8) * items.size() / (2 * Math.PI), 43.0);
		double progress = Math.min(1.0, (age + tickDelta) / 5.0);
		radius *= 1 - (1 - progress) * (1 - progress);
		renderBackground();
		for (int index = 0; index < items.size(); index++) {
			double angle = 2 * Math.PI * index / items.size() - Math.PI / 2;
			int x = centerX + (int) Math.round(Math.cos(angle) * radius) - BUTTON_SIZE / 2;
			int y = centerY + (int) Math.round(Math.sin(angle) * radius) - BUTTON_SIZE / 2;
			GL11.glColor4f(1, 1, 1, 1);
			minecraft.getTextureManager().bind(TEXTURE);
			drawTexture(x, y, index == selectedButton ? 16 : 0, 0, 16, 16, BUTTON_SIZE, BUTTON_SIZE, 32, 16);
			drawCenteredString(textRenderer, items.get(index).icon, x + BUTTON_SIZE / 2, y + 12, 0xFFFFFF);
		}
		if (selectedButton >= 0) {
			drawCenteredString(textRenderer, items.get(selectedButton).label, centerX, centerY - 4, 0xFFFFFF);
		}
		drawCenteredString(textRenderer, "Right stick to select · release " + ControllerAction.RADIAL_MENU.label(),
				centerX, height - 33, 0xFFFFFF);
		drawCenteredString(textRenderer, "LB / RB    " + (pageIndex + 1) + " / " + PAGES.size(),
				centerX, height - 19, 0xCCCCCC);
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}

	enum Action {
		HUD("HUD", "Toggle HUD"), PERSPECTIVE("3P", "Change perspective"), DROP_STACK("Drop", "Drop stack"),
		CHAT("Chat", "Open chat"), PICK_BLOCK("Pick", "Pick block"), SCREENSHOT("Shot", "Take screenshot"),
		PLAYER_LIST("List", "Player list"), DEBUG("F3", "Debug overlay"), PROFILER("Pie", "Profiler chart"),
		TPS("TPS", "TPS chart"), RELOAD_CHUNKS("Chunk", "Reload chunks"), TOOLTIPS("Tips", "Advanced tooltips"),
		RELOAD_RESOURCES("Packs", "Reload resource packs"), CLEAR_CHAT("Clear", "Clear chat");

		private final String icon;
		private final String label;

		Action(String icon, String label) {
			this.icon = icon;
			this.label = label;
		}
	}
}
