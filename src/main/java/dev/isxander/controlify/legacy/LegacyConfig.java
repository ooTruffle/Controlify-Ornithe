package dev.isxander.controlify.legacy;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public final class LegacyConfig extends Config {
	private static final int MIN_CAMERA_SPEED = 25;
	private static final int MAX_CAMERA_SPEED = 300;

	public static final LegacyConfig INSTANCE = new LegacyConfig();

	@Slider(
			title = "Controller Camera Speed (%)",
			category = "Controller",
			description = "Adjust how fast the right stick turns the camera. 100% is the default speed.",
			min = MIN_CAMERA_SPEED,
			max = MAX_CAMERA_SPEED,
			step = 5
	)
	private static int cameraSpeedPercent = 100;

	@Switch(
			title = "Gyro Aim",
			category = "Controller",
			description = "Turn the camera by moving a controller that exposes a gyroscope."
	)
	private static boolean gyroAim;

	@Slider(
			title = "Gyro Sensitivity (%)",
			category = "Controller",
			description = "Adjust how much the camera turns when you move the controller.",
			min = MIN_CAMERA_SPEED,
			max = MAX_CAMERA_SPEED,
			step = 5
	)
	private static int gyroSensitivityPercent = 100;

	@Slider(title = "Left Stick Deadzone (%)", category = "Controller", min = 0, max = 50)
	private static int leftStickDeadzone = 18;

	@Slider(title = "Right Stick Deadzone (%)", category = "Controller", min = 0, max = 50)
	private static int rightStickDeadzone = 18;

	@Switch(title = "Vibration", category = "Controller", description = "Vibrate when your player takes damage.")
	static boolean vibration = true;

	@Button(title = "Test Vibration", category = "Controller", text = "Test",
			description = "Play a short pulse on the active controller.")
	public void testVibration() {
		ControllerManager.rumble(0.65F, 0.4F, 250);
	}

	@Dropdown(title = "Attack", category = "Gameplay Bindings")
	static ControllerButton attack = ControllerButton.RIGHT_TRIGGER;

	@Dropdown(title = "Use item", category = "Gameplay Bindings")
	static ControllerButton use = ControllerButton.LEFT_TRIGGER;

	@Dropdown(title = "Jump", category = "Gameplay Bindings")
	static ControllerButton jump = ControllerButton.A;

	@Dropdown(title = "Sneak", category = "Gameplay Bindings")
	static ControllerButton sneak = ControllerButton.B;

	@Dropdown(title = "Sprint", category = "Gameplay Bindings")
	static ControllerButton sprint = ControllerButton.LEFT_STICK;

	@Dropdown(title = "Inventory", category = "Gameplay Bindings")
	static ControllerButton inventory = ControllerButton.Y;

	@Dropdown(title = "Drop item", category = "Gameplay Bindings")
	static ControllerButton drop = ControllerButton.X;

	@Dropdown(title = "Previous hotbar slot", category = "Gameplay Bindings")
	static ControllerButton hotbarPrevious = ControllerButton.LEFT_BUMPER;

	@Dropdown(title = "Next hotbar slot", category = "Gameplay Bindings")
	static ControllerButton hotbarNext = ControllerButton.RIGHT_BUMPER;

	@Dropdown(title = "Open chat", category = "Gameplay Bindings")
	static ControllerButton chat = ControllerButton.DPAD_UP;

	@Dropdown(title = "Cycle perspective", category = "Gameplay Bindings")
	static ControllerButton perspective = ControllerButton.DPAD_DOWN;

	@Dropdown(title = "Player list", category = "Gameplay Bindings")
	static ControllerButton playerList = ControllerButton.DPAD_RIGHT;

	@Dropdown(title = "Pick block", category = "Gameplay Bindings")
	static ControllerButton pickBlock = ControllerButton.RIGHT_STICK;

	@Dropdown(title = "Pause / Resume", category = "Gameplay Bindings")
	static ControllerButton pause = ControllerButton.START;

	@Dropdown(title = "Left click", category = "Menu Bindings")
	static ControllerButton menuSelect = ControllerButton.A;

	@Dropdown(title = "Right click", category = "Menu Bindings")
	static ControllerButton menuRightClick = ControllerButton.X;

	@Dropdown(title = "Go back", category = "Menu Bindings")
	static ControllerButton menuBack = ControllerButton.B;

	@Dropdown(title = "Scroll up", category = "Menu Bindings")
	static ControllerButton menuScrollUp = ControllerButton.LEFT_BUMPER;

	@Dropdown(title = "Scroll down", category = "Menu Bindings")
	static ControllerButton menuScrollDown = ControllerButton.RIGHT_BUMPER;

	@Dropdown(title = "Quick move item stack", category = "Menu Bindings")
	static ControllerButton quickMove = ControllerButton.Y;

	@Switch(
			title = "Show controller guide",
			category = "Controller",
			description = "Show controller controls on screen while playing and in menus."
	)
	public static boolean showControllerGuide = true;

	static float cameraSpeedMultiplier() {
		return Math.clamp(cameraSpeedPercent, MIN_CAMERA_SPEED, MAX_CAMERA_SPEED) / 100.0F;
	}

	static float gyroAimMultiplier() {
		return gyroAim ? Math.clamp(gyroSensitivityPercent, MIN_CAMERA_SPEED, MAX_CAMERA_SPEED) / 100.0F : 0.0F;
	}

	static float leftStickDeadzone() {
		return Math.clamp(leftStickDeadzone, 0, 50) / 100.0F;
	}

	static float rightStickDeadzone() {
		return Math.clamp(rightStickDeadzone, 0, 50) / 100.0F;
	}

	private LegacyConfig() {
		super("controlify-legacy.json", "Controlify", Category.UTILITY);
	}
}
