package dev.isxander.controlify.legacy;

import dev.isxander.controlify.controller.input.GamepadInputs;
import net.minecraft.resource.Identifier;

import static org.lwjgl.sdl.SDLGamepad.*;

enum ControllerButton {
	UNBOUND("Unbound", 0, null),
	A("A", 1 << SDL_GAMEPAD_BUTTON_SOUTH, GamepadInputs.SOUTH_BUTTON),
	B("B", 1 << SDL_GAMEPAD_BUTTON_EAST, GamepadInputs.EAST_BUTTON),
	X("X", 1 << SDL_GAMEPAD_BUTTON_WEST, GamepadInputs.WEST_BUTTON),
	Y("Y", 1 << SDL_GAMEPAD_BUTTON_NORTH, GamepadInputs.NORTH_BUTTON),
	LEFT_BUMPER("LB", 1 << SDL_GAMEPAD_BUTTON_LEFT_SHOULDER, GamepadInputs.LEFT_SHOULDER_BUTTON),
	RIGHT_BUMPER("RB", 1 << SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER, GamepadInputs.RIGHT_SHOULDER_BUTTON),
	// Reserve two bits above the digital button IDs for trigger presses.
	LEFT_TRIGGER("LT", 1 << 30, null),
	RIGHT_TRIGGER("RT", 1 << 31, null),
	LEFT_STICK("L3", 1 << SDL_GAMEPAD_BUTTON_LEFT_STICK, GamepadInputs.LEFT_STICK_BUTTON),
	RIGHT_STICK("R3", 1 << SDL_GAMEPAD_BUTTON_RIGHT_STICK, GamepadInputs.RIGHT_STICK_BUTTON),
	DPAD_UP("D-Pad Up", 1 << SDL_GAMEPAD_BUTTON_DPAD_UP, GamepadInputs.DPAD_UP_BUTTON),
	DPAD_DOWN("D-Pad Down", 1 << SDL_GAMEPAD_BUTTON_DPAD_DOWN, GamepadInputs.DPAD_DOWN_BUTTON),
	DPAD_LEFT("D-Pad Left", 1 << SDL_GAMEPAD_BUTTON_DPAD_LEFT, GamepadInputs.DPAD_LEFT_BUTTON),
	DPAD_RIGHT("D-Pad Right", 1 << SDL_GAMEPAD_BUTTON_DPAD_RIGHT, GamepadInputs.DPAD_RIGHT_BUTTON),
	START("Start", 1 << SDL_GAMEPAD_BUTTON_START, GamepadInputs.START_BUTTON),
	VIEW("View", 1 << SDL_GAMEPAD_BUTTON_BACK, GamepadInputs.BACK_BUTTON);

	static final ControllerButton[] DIGITAL_BUTTONS = java.util.Arrays.stream(values())
			.filter(button -> button.input != null)
			.toArray(ControllerButton[]::new);

	final String label;
	final int mask;
	final Identifier input;

	ControllerButton(String label, int mask, Identifier input) {
		this.label = label;
		this.mask = mask;
		this.input = input;
	}

	@Override
	public String toString() {
		return label;
	}
}
