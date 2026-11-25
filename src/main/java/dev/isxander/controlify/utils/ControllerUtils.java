/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

public final class ControllerUtils {
	private ControllerUtils() {
	}

	public static float deadzone(float value, float deadzone) {
		return (value - Math.copySign(Math.min(deadzone, Math.abs(value)), value)) / (1 - deadzone);
	}
}
