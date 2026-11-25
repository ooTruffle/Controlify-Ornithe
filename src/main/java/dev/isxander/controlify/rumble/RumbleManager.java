/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.rumble;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Consumer;

public class RumbleManager {
	private final Consumer<RumbleState> output;
	private final Queue<RumbleEffect> effectQueue;

	private boolean silent, wasSilent;

	public RumbleManager(Consumer<RumbleState> output) {
		this.output = output;
		this.effectQueue = new PriorityQueue<>();
	}

	public void play(RumbleEffect effect) {
		effectQueue.add(effect);
	}

	public void tick() {
		effectQueue.removeIf(RumbleEffect::isFinished);
		effectQueue.forEach(RumbleEffect::tick);

		if (effectQueue.isEmpty()) {
			clearRumble();
			return;
		}

		float strong = 0f, weak = 0f;
		for (RumbleEffect effect : effectQueue) {
			RumbleState effectState = effect.currentState();
			strong = Math.max(strong, effectState.strong());
			weak = Math.max(weak, effectState.weak());
		}
		RumbleState state = new RumbleState(strong, weak);

		if (state.isZero()) {
			clearRumble();
			return;
		}

		if (silent) {
			clearRumble();
		} else {
			output.accept(state);
			wasSilent = false;
		}
	}

	private void clearRumble() {
		if (wasSilent)
			return;

		output.accept(RumbleState.NONE);
		wasSilent = true;
	}

	public void clearEffects() {
		effectQueue.clear();
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isPlaying() {
		return !effectQueue.isEmpty();
	}
}
