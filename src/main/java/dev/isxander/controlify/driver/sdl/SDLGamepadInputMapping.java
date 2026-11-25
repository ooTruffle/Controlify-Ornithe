/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import net.minecraft.resource.Identifier;
import org.lwjgl.sdl.SDLGamepad;

public sealed interface SDLGamepadInputMapping {
	Identifier controlifyInput();

	boolean hasInput(long gamepad);

	void applyState(long gamepad, ControllerStateImpl state);

	record ButtonMapping(Identifier controlifyInput, int sdlButton) implements SDLGamepadInputMapping {
		@Override
		public boolean hasInput(long gamepad) {
			return SDLGamepad.SDL_GamepadHasButton(gamepad, sdlButton);
		}

		@Override
		public void applyState(long gamepad, ControllerStateImpl state) {
			if (hasInput(gamepad)) {
				state.setButton(controlifyInput, SDLGamepad.SDL_GetGamepadButton(gamepad, sdlButton));
			}
		}
	}

	record AxisMapping(Identifier controlifyInput, int sdlAxis, boolean positive) implements SDLGamepadInputMapping {
		@Override
		public boolean hasInput(long gamepad) {
			return SDLGamepad.SDL_GamepadHasAxis(gamepad, sdlAxis);
		}

		@Override
		public void applyState(long gamepad, ControllerStateImpl state) {
			if (hasInput(gamepad)) {
				short raw = SDLGamepad.SDL_GetGamepadAxis(gamepad, sdlAxis);
				// SDL's signed range has one extra negative value.
				float f = raw < 0 ? raw / 32768.0F : raw / 32767.0F;
				f = Math.max(0.0F, positive ? f : -f);

				state.setAxis(controlifyInput, f);
			}
		}
	}
}
