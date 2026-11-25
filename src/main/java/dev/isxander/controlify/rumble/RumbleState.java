/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.rumble;

public record RumbleState(float strong, float weak) {
	public static final RumbleState NONE = new RumbleState(0.0F, 0.0F);

	public boolean isZero() {
		return strong == 0.0F && weak == 0.0F;
	}
}
