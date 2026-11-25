/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.ingame;

import net.minecraft.client.entity.living.player.Input;

/** Adapts ControllerPlayerMovement and DualInput to Feather's mutable movement fields. */
public final class ControllerPlayerMovement {
	private ControllerPlayerMovement() {
	}

	public static void combineWithKeyboard(Input input, float forward, float left) {
		if (forward == 0.0F && left == 0.0F) return;

		// Limit controller movement to length 1 while retaining partial stick deflection.
		float length = (float) Math.hypot(forward, left);
		if (length > 1.0F) {
			forward /= length;
			left /= length;
		}

		// KeyboardInput has already applied the vanilla sneak multiplier to the keyboard input.
		float speed = input.sneaking ? 0.3F : 1.0F;
		input.movementForward = Math.clamp(input.movementForward + forward * speed, -speed, speed);
		input.movementSideways = Math.clamp(input.movementSideways + left * speed, -speed, speed);
	}
}
