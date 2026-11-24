package dev.isxander.controlify.legacy;

import static org.lwjgl.sdl.SDLGamepad.*;

enum ControllerButton {
	UNBOUND("Unbound", 0),
	A("A", 1 << SDL_GAMEPAD_BUTTON_SOUTH),
	B("B", 1 << SDL_GAMEPAD_BUTTON_EAST),
	X("X", 1 << SDL_GAMEPAD_BUTTON_WEST),
	Y("Y", 1 << SDL_GAMEPAD_BUTTON_NORTH),
	LEFT_BUMPER("LB", 1 << SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
	RIGHT_BUMPER("RB", 1 << SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
	// Reserve two bits above the digital button IDs for trigger presses.
	LEFT_TRIGGER("LT", 1 << 30),
	RIGHT_TRIGGER("RT", 1 << 31),
	LEFT_STICK("L3", 1 << SDL_GAMEPAD_BUTTON_LEFT_STICK),
	RIGHT_STICK("R3", 1 << SDL_GAMEPAD_BUTTON_RIGHT_STICK),
	DPAD_UP("D-Pad Up", 1 << SDL_GAMEPAD_BUTTON_DPAD_UP),
	DPAD_DOWN("D-Pad Down", 1 << SDL_GAMEPAD_BUTTON_DPAD_DOWN),
	DPAD_LEFT("D-Pad Left", 1 << SDL_GAMEPAD_BUTTON_DPAD_LEFT),
	DPAD_RIGHT("D-Pad Right", 1 << SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
	START("Start", 1 << SDL_GAMEPAD_BUTTON_START),
	VIEW("View", 1 << SDL_GAMEPAD_BUTTON_BACK);

	static final ControllerButton[] DIGITAL_BUTTONS = java.util.Arrays.stream(values())
			.filter(button -> button != UNBOUND && button != LEFT_TRIGGER && button != RIGHT_TRIGGER)
			.toArray(ControllerButton[]::new);

	final String label;
	final int mask;

	ControllerButton(String label, int mask) {
		this.label = label;
		this.mask = mask;
	}

	@Override
	public String toString() {
		return label;
	}
}
