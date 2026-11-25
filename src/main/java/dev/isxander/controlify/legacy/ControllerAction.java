package dev.isxander.controlify.legacy;

import java.util.function.Supplier;

enum ControllerAction {
	ATTACK(() -> LegacyConfig.attack),
	USE(() -> LegacyConfig.use),
	JUMP(() -> LegacyConfig.jump),
	SNEAK(() -> LegacyConfig.sneak),
	SPRINT(() -> LegacyConfig.sprint),
	INVENTORY(() -> LegacyConfig.inventory),
	DROP(() -> LegacyConfig.drop),
	HOTBAR_PREV(() -> LegacyConfig.hotbarPrevious),
	HOTBAR_NEXT(() -> LegacyConfig.hotbarNext),
	CHAT(() -> LegacyConfig.chat),
	PERSPECTIVE(() -> LegacyConfig.perspective),
	PLAYER_LIST(() -> LegacyConfig.playerList),
	PICK_BLOCK(() -> LegacyConfig.pickBlock),
	PAUSE(() -> LegacyConfig.pause),
	RADIAL_MENU(() -> LegacyConfig.radialMenu),
	GYRO(() -> LegacyConfig.gyroButton),
	MENU_SELECT(() -> LegacyConfig.menuSelect),
	MENU_RIGHT_CLICK(() -> LegacyConfig.menuRightClick),
	MENU_BACK(() -> LegacyConfig.menuBack),
	MENU_SCROLL_UP(() -> LegacyConfig.menuScrollUp),
	MENU_SCROLL_DOWN(() -> LegacyConfig.menuScrollDown),
	QUICK_MOVE(() -> LegacyConfig.quickMove);

	private final Supplier<ControllerButton> binding;

	ControllerAction(Supplier<ControllerButton> binding) {
		this.binding = binding;
	}

	int buttonMask() {
		return binding.get().mask;
	}

	String label() {
		return binding.get().label;
	}
}
