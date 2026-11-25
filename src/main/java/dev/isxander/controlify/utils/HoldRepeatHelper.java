/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

/** Upstream's initial delay and repeat delay, with digital state supplied by the legacy bindings. */
public final class HoldRepeatHelper {
	private final int initialDelay;
	private final int repeatDelay;
	private int currentDelay;
	private boolean wasHeld;

	public HoldRepeatHelper(int initialDelay, int repeatDelay) {
		this.initialDelay = initialDelay;
		this.repeatDelay = repeatDelay;
	}

	public boolean shouldAction(boolean held) {
		boolean pressed = held && !wasHeld;
		wasHeld = held;
		if (pressed || (held && --currentDelay <= 0)) {
			currentDelay = pressed ? initialDelay : repeatDelay;
			return true;
		}
		return false;
	}

	public void reset() {
		wasHeld = false;
		currentDelay = 0;
	}
}
