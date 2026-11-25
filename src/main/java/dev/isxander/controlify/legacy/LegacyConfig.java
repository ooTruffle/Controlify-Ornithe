package dev.isxander.controlify.legacy;

import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroYawMode;
import dev.isxander.controlify.ingame.InputCurves;
import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Include;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public final class LegacyConfig extends Config {
	private static final int MIN_CAMERA_SPEED = 25;
	private static final int MAX_CAMERA_SPEED = 300;

	public static final LegacyConfig INSTANCE = new LegacyConfig();

	@Include
	static String preferredController = "";

	@Include
	static String controllerProfilesJson = "{}";

	@Button(title = "Active Controller", category = "Controller", text = "Choose Controller",
			description = "Choose a connected controller or use automatic selection.")
	public void chooseController() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.executeTask(() -> minecraft.openScreen(new ControllerPickerScreen(minecraft.screen)));
	}

	@Slider(
			title = "Controller Camera Speed (%)",
			category = "Controller",
			description = "Adjust how fast the right stick turns the camera. 100% is the default speed.",
			min = MIN_CAMERA_SPEED,
			max = MAX_CAMERA_SPEED,
			step = 5
	)
	private static int cameraSpeedPercent = 100;

	@Slider(title = "Vertical Camera Speed (%)", category = "Controller", min = 25, max = 300, step = 5)
	private static int verticalCameraSpeedPercent = 90;

	@Dropdown(title = "Look Input Curve", category = "Controller")
	static InputCurves lookCurve = InputCurves.STANDARD;

	@Switch(title = "Invert Controller Look Horizontally", category = "Controller")
	static boolean invertHorizontalLook;

	@Switch(title = "Invert Controller Look Vertically", category = "Controller")
	static boolean invertVerticalLook;

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

	@Dropdown(title = "Gyro Activation Mode", category = "Controller",
			description = "On uses the activation binding, Invert temporarily disables gyro, Toggle switches gyro on or off, and Off disables the activation binding.")
	static GyroButtonMode gyroButtonMode = GyroButtonMode.ON;

	@Dropdown(title = "Gyro Yaw Axis", category = "Controller")
	static GyroYawMode gyroYawMode = GyroYawMode.YAW;

	@Switch(title = "Invert Gyro Pitch", category = "Controller")
	static boolean invertGyroPitch;

	@Switch(title = "Invert Gyro Yaw", category = "Controller")
	static boolean invertGyroYaw;

	@Slider(title = "Left Stick Deadzone (%)", category = "Controller", min = 0, max = 50)
	private static int leftStickDeadzone = 12;

	@Slider(title = "Right Stick Deadzone (%)", category = "Controller", min = 0, max = 50)
	private static int rightStickDeadzone = 12;

	@Slider(title = "Digital Activation Threshold (%)", category = "Controller", min = 1, max = 100)
	private static int activationThreshold = 50;

	@Switch(title = "Toggle Sneak", category = "Controller")
	static boolean toggleSneak = true;

	@Switch(title = "Toggle Sprint", category = "Controller")
	static boolean toggleSprint = true;

	@Switch(title = "On-Screen Chat Keyboard", category = "Controller")
	static boolean onScreenKeyboard = true;

	@Switch(title = "Reduce Bow Sensitivity", category = "Controller")
	static boolean reduceBowSensitivity = true;

	@Switch(title = "Vibration", category = "Controller", description = "Play vibration effects for damage, explosions, and item use.")
	static boolean vibration = true;

	@Button(title = "Test Vibration", category = "Controller", text = "Test",
			description = "Play a short pulse on the active controller.")
	public void testVibration() {
		Minecraft.getInstance().executeTask(ControllerManager::testRumble);
	}

	@Dropdown(title = "Attack", category = "Gameplay Bindings")
	static ControllerButton attack = DefaultBindings.get("attack");

	@Dropdown(title = "Use item", category = "Gameplay Bindings")
	static ControllerButton use = DefaultBindings.get("use");

	@Dropdown(title = "Jump", category = "Gameplay Bindings")
	static ControllerButton jump = DefaultBindings.get("jump");

	@Dropdown(title = "Sneak", category = "Gameplay Bindings")
	static ControllerButton sneak = DefaultBindings.get("sneak");

	@Dropdown(title = "Sprint", category = "Gameplay Bindings")
	static ControllerButton sprint = DefaultBindings.get("sprint");

	@Dropdown(title = "Inventory", category = "Gameplay Bindings")
	static ControllerButton inventory = DefaultBindings.get("inventory");

	@Dropdown(title = "Drop item", category = "Gameplay Bindings")
	static ControllerButton drop = DefaultBindings.get("drop");

	@Dropdown(title = "Previous hotbar slot", category = "Gameplay Bindings")
	static ControllerButton hotbarPrevious = DefaultBindings.get("prev_slot");

	@Dropdown(title = "Next hotbar slot", category = "Gameplay Bindings")
	static ControllerButton hotbarNext = DefaultBindings.get("next_slot");

	@Dropdown(title = "Open chat", category = "Gameplay Bindings")
	static ControllerButton chat = DefaultBindings.get("open_chat");

	@Dropdown(title = "Cycle perspective", category = "Gameplay Bindings")
	static ControllerButton perspective = DefaultBindings.get("change_perspective");

	@Dropdown(title = "Player list", category = "Gameplay Bindings")
	static ControllerButton playerList = DefaultBindings.get("show_player_list");

	@Dropdown(title = "Pick block", category = "Gameplay Bindings")
	static ControllerButton pickBlock = DefaultBindings.get("pick_block");

	@Dropdown(title = "Pause / Resume", category = "Gameplay Bindings")
	static ControllerButton pause = DefaultBindings.get("pause");

	@Dropdown(title = "Radial menu", category = "Gameplay Bindings")
	static ControllerButton radialMenu = DefaultBindings.get("radial_menu");

	@Dropdown(title = "Gyro activation", category = "Gameplay Bindings")
	static ControllerButton gyroButton = DefaultBindings.get("gyro");

	@Dropdown(title = "Left click", category = "Menu Bindings")
	static ControllerButton menuSelect = DefaultBindings.get("vmouse_lclick");

	@Dropdown(title = "Right click", category = "Menu Bindings")
	static ControllerButton menuRightClick = DefaultBindings.get("vmouse_rclick");

	@Dropdown(title = "Go back", category = "Menu Bindings")
	static ControllerButton menuBack = DefaultBindings.get("gui_back");

	@Dropdown(title = "Extra scroll up button", category = "Menu Bindings")
	static ControllerButton menuScrollUp = DefaultBindings.get("vmouse_scroll_up");

	@Dropdown(title = "Extra scroll down button", category = "Menu Bindings")
	static ControllerButton menuScrollDown = DefaultBindings.get("vmouse_scroll_down");

	@Dropdown(title = "Quick move item stack", category = "Menu Bindings")
	static ControllerButton quickMove = DefaultBindings.get("inv_quick_move");

	@Switch(
			title = "Show controller guide",
			category = "Controller",
			description = "Show controller controls on screen while playing and in menus."
	)
	public static boolean showControllerGuide = true;

	@Button(title = "Restore Upstream Defaults", category = "Controller", text = "Restore",
			description = "Reset controller bindings and stick settings to Controlify's upstream defaults.")
	public void restoreDefaults() {
		Minecraft.getInstance().executeTask(() -> {
			applyUpstreamInputDefaults();
			ControllerManager.suspendGameplayButtons();
			save();
		});
	}

	static void applyUpstreamInputDefaults() {
		attack = DefaultBindings.get("attack");
		use = DefaultBindings.get("use");
		jump = DefaultBindings.get("jump");
		sneak = DefaultBindings.get("sneak");
		sprint = DefaultBindings.get("sprint");
		inventory = DefaultBindings.get("inventory");
		drop = DefaultBindings.get("drop");
		hotbarPrevious = DefaultBindings.get("prev_slot");
		hotbarNext = DefaultBindings.get("next_slot");
		chat = DefaultBindings.get("open_chat");
		perspective = DefaultBindings.get("change_perspective");
		playerList = DefaultBindings.get("show_player_list");
		pickBlock = DefaultBindings.get("pick_block");
		pause = DefaultBindings.get("pause");
		menuSelect = DefaultBindings.get("vmouse_lclick");
		menuRightClick = DefaultBindings.get("vmouse_rclick");
		menuBack = DefaultBindings.get("gui_back");
		menuScrollUp = DefaultBindings.get("vmouse_scroll_up");
		menuScrollDown = DefaultBindings.get("vmouse_scroll_down");
		quickMove = DefaultBindings.get("inv_quick_move");
		radialMenu = DefaultBindings.get("radial_menu");
		gyroButton = DefaultBindings.get("gyro");
		leftStickDeadzone = rightStickDeadzone = 12;
		activationThreshold = 50;
		cameraSpeedPercent = 100;
		verticalCameraSpeedPercent = 90;
		invertHorizontalLook = invertVerticalLook = false;
		lookCurve = InputCurves.STANDARD;
		toggleSneak = toggleSprint = reduceBowSensitivity = true;
	}

	static void applyUpstreamProfileDefaults() {
		applyUpstreamInputDefaults();
		gyroAim = false;
		gyroSensitivityPercent = 100;
		gyroButtonMode = GyroButtonMode.ON;
		gyroYawMode = GyroYawMode.YAW;
		invertGyroPitch = invertGyroYaw = false;
		onScreenKeyboard = true;
		vibration = true;
		showControllerGuide = true;
	}

	static float activationThreshold() {
		return Math.clamp(activationThreshold, 1, 100) / 100.0F;
	}

	static float cameraSpeedMultiplier() {
		return Math.clamp(cameraSpeedPercent, MIN_CAMERA_SPEED, MAX_CAMERA_SPEED) / 100.0F
				* (invertHorizontalLook ? -1 : 1);
	}

	static float verticalCameraSpeedMultiplier() {
		return Math.clamp(verticalCameraSpeedPercent, MIN_CAMERA_SPEED, MAX_CAMERA_SPEED) / 100.0F
				* (invertVerticalLook ? -1 : 1);
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
