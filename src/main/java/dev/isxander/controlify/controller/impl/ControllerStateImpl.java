/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.impl;

import net.minecraft.resource.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ControllerStateImpl {
	private final Map<Identifier, Boolean> buttons = new HashMap<>();
	private final Map<Identifier, Float> axes = new HashMap<>();

	public boolean isButtonDown(Identifier button) {
		return buttons.getOrDefault(button, false);
	}

	public float getAxisState(Identifier axis) {
		return axes.getOrDefault(axis, 0f);
	}

	public boolean isGivingInput() {
		return buttons.containsValue(true) || axes.values().stream().anyMatch(axis -> Math.abs(axis) > 0.5F);
	}

	public void setButton(Identifier button, boolean value) {
		buttons.put(button, value);
	}

	public void setAxis(Identifier axis, float value) {
		axes.put(axis, value);
	}

	public void clearState() {
		buttons.clear();
		axes.clear();
	}
}
