package dev.isxander.controlify.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** SDL 3 gamepad input for Ornithe's LWJGL 3 runtime (provided by Pylon). */
public final class ControllerManager {
	static final float DEADZONE = 0.18F;
	private static final int[] ACTION_BUTTONS = {
			SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH,
			SDLGamepad.SDL_GAMEPAD_BUTTON_WEST,
			SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER,
			SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER,
			SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP,
			SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN,
			SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT,
			SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK,
			SDLGamepad.SDL_GAMEPAD_BUTTON_START
	};
	private static boolean initialized;
	private static boolean unavailable;
	private static long gamepad;
	private static String gamepadName;
	private static boolean noGamepadLogged;
	private static int rescanTicks;
	private static boolean previousConfirm;
	private static boolean previousBack;
	private static int actionButtonsDown;
	private static int actionButtonsPressed;
	private static boolean sampledInGameplay;

	private static final ControllerState STATE = new ControllerState();
	private static final ControllerLook LOOK = new ControllerLook();
	private static final ControllerMenu MENU = new ControllerMenu();
	private static final TriggerInput ATTACK = new TriggerInput();
	private static final TriggerInput USE = new TriggerInput();

	private ControllerManager() {
	}

	public static void tick(Minecraft minecraft) {
		if (minecraft.screen == null) MENU.releaseButton();
		if (unavailable) {
			return;
		}
		if (!initialized) {
			if (!SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_GAMEPAD)) {
				unavailable = true;
				ControlifyLegacy.LOGGER.error("Could not initialize SDL gamepad input: {}", SDLError.SDL_GetError());
				return;
			}
			initialized = true;
			ControlifyLegacy.LOGGER.info("SDL 3 gamepad input initialized");
		}

		SDLGamepad.SDL_UpdateGamepads();
		if (gamepad != 0L && !SDLGamepad.SDL_GamepadConnected(gamepad)) {
			ControlifyLegacy.LOGGER.info("Disconnected controller: {}", gamepadName);
			SDLGamepad.SDL_CloseGamepad(gamepad);
			gamepad = 0L;
			gamepadName = null;
			rescanTicks = 0;
			STATE.clear();
			ATTACK.update(false, false);
			USE.update(false, false);
			LOOK.reset();
			actionButtonsDown = actionButtonsPressed = 0;
		}
		if (gamepad == 0L) {
			if (rescanTicks-- <= 0) {
				openFirstGamepad();
				rescanTicks = 20;
			}
		}

		if (gamepad == 0L) {
			return;
		}

		previousConfirm = STATE.jump;
		previousBack = STATE.sneak;
		readState();
		sampledInGameplay = isGameplayActive(minecraft);
		ATTACK.update(STATE.rightTrigger > DEADZONE, sampledInGameplay);
		USE.update(STATE.leftTrigger > DEADZONE, sampledInGameplay);
		int buttons = readActionButtons();
		actionButtonsPressed = buttons & ~actionButtonsDown;
		actionButtonsDown = buttons;
		if (sampledInGameplay) {
			handleGameplayButtons(minecraft);
		}
	}

	public static void updateFrame(Minecraft minecraft) {
		boolean gameplay = isGameplayActive(minecraft);
		boolean menu = minecraft.screen != null && canNavigateMenu();
		if (!gameplay) LOOK.reset();
		if (!menu) MENU.resetMotion();
		if (!gameplay && !menu) return;

		SDLGamepad.SDL_UpdateGamepads();
		if (!SDLGamepad.SDL_GamepadConnected(gamepad)) {
			LOOK.reset();
			MENU.resetMotion();
			return;
		}
		if (menu) {
			float x = menuAxis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX,
					SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT, SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT);
			float y = menuAxis(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY,
					SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP, SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN);
			MENU.moveCursor(x, y);
			return;
		}

		// Apply right-stick look before the renderer interpolates the camera.
		float x = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX));
		float y = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY));
		LOOK.update(x, y, System.nanoTime(), minecraft.options.mouseSensitivity,
				LegacyConfig.cameraSpeedMultiplier(), minecraft.options.invertMouseY);
		if (LOOK.turnX != 0.0F || LOOK.turnY != 0.0F) {
			minecraft.player.updateLocalPlayerCamera(LOOK.turnX, LOOK.turnY);
		}
	}

	private static float menuAxis(int axis, int negativeButton, int positiveButton) {
		boolean negative = SDLGamepad.SDL_GetGamepadButton(gamepad, negativeButton);
		boolean positive = SDLGamepad.SDL_GetGamepadButton(gamepad, positiveButton);
		if (negative || positive) return Boolean.compare(positive, negative);
		return applyDeadzone(normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, axis)));
	}

	public static boolean canNavigateMenu() {
		return isConnected() && Display.isActive() && Mouse.isCreated() && !Mouse.isGrabbed();
	}

	public static void handleMenuInput(Screen screen) {
		boolean leftClick = !sampledInGameplay && STATE.jump && !previousConfirm;
		boolean rightClick = !sampledInGameplay && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST);
		boolean scrollUp = !sampledInGameplay && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
		boolean scrollDown = !sampledInGameplay && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
		MENU.handleInput(screen, leftClick, STATE.jump, rightClick,
				isActionHeld(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST), Boolean.compare(scrollUp, scrollDown));
	}

	public static void releaseMenuButton() {
		MENU.releaseButton();
	}

	private static int readActionButtons() {
		int buttons = 0;
		for (int button : ACTION_BUTTONS) {
			if (SDLGamepad.SDL_GetGamepadButton(gamepad, button)) {
				buttons |= 1 << button;
			}
		}
		return buttons;
	}

	private static boolean consumeActionPress(int button) {
		int mask = 1 << button;
		boolean pressed = (actionButtonsPressed & mask) != 0;
		actionButtonsPressed &= ~mask;
		return pressed;
	}

	public static boolean consumePausePress() {
		return consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_START);
	}

	private static void handleGameplayButtons(Minecraft minecraft) {
		if (consumePausePress()) {
			minecraft.pauseGame();
			actionButtonsPressed = 0;
			return;
		}

		boolean previousSlot = consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
		boolean nextSlot = consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
		int scroll = Boolean.compare(previousSlot, nextSlot);
		if (minecraft.player.isSpectator()) {
			var spectatorGui = minecraft.gui.getSpectatorGui();
			if (consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK)) {
				spectatorGui.mouseMiddleClicked();
			}
			if (scroll != 0 && spectatorGui.isMenuActive()) spectatorGui.mouseScrolled(-scroll);
		} else if (scroll != 0) {
			minecraft.player.inventory.scrollInHotbar(scroll);
		}

		if (consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN)) {
			// Apply the same shader and world-renderer updates as Minecraft's perspective key.
			int perspective = (minecraft.options.perspective + 1) % 3;
			minecraft.options.perspective = perspective;
			if (perspective == 0) minecraft.gameRenderer.updateShader(minecraft.getCamera());
			if (perspective == 1) minecraft.gameRenderer.updateShader(null);
			minecraft.worldRenderer.onViewChanged();
		}
	}

	private static void openFirstGamepad() {
		IntBuffer gamepads = SDLGamepad.SDL_GetGamepads();
		if (gamepads == null) {
			logNoGamepadOnce();
			return;
		}
		try {
			if (!gamepads.hasRemaining()) {
				logNoGamepadOnce();
			}
			for (int i = 0; i < gamepads.remaining(); i++) {
				int id = gamepads.get(gamepads.position() + i);
				long candidate = SDLGamepad.SDL_OpenGamepad(id);
				if (candidate != 0L) {
					gamepad = candidate;
					gamepadName = SDLGamepad.SDL_GetGamepadName(candidate);
					noGamepadLogged = false;
					configureSensors();
					readState();
					actionButtonsDown = readActionButtons();
					ATTACK.update(STATE.rightTrigger > DEADZONE, false);
					USE.update(STATE.leftTrigger > DEADZONE, false);
					ControlifyLegacy.LOGGER.info("Connected controller: {}", gamepadName);
					return;
				}
			}
		} finally {
			SDLStdinc.SDL_free(gamepads);
		}
	}

	private static void logNoGamepadOnce() {
		if (!noGamepadLogged) {
			ControlifyLegacy.LOGGER.info("No SDL gamepads connected");
			noGamepadLogged = true;
		}
	}

	private static void configureSensors() {
		STATE.gyroAvailable = SDLGamepad.SDL_GamepadHasSensor(gamepad, SDLSensor.SDL_SENSOR_GYRO);
		if (STATE.gyroAvailable) {
			SDLGamepad.SDL_SetGamepadSensorEnabled(gamepad, SDLSensor.SDL_SENSOR_GYRO, true);
		}
		STATE.accelAvailable = SDLGamepad.SDL_GamepadHasSensor(gamepad, SDLSensor.SDL_SENSOR_ACCEL);
		if (STATE.accelAvailable) {
			SDLGamepad.SDL_SetGamepadSensorEnabled(gamepad, SDLSensor.SDL_SENSOR_ACCEL, true);
		}
	}

	private static void readState() {
		STATE.leftX = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX));
		STATE.leftY = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY));
		STATE.rightX = applyDeadzone(normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX)));
		STATE.rightY = applyDeadzone(normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY)));
		STATE.leftTrigger = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER));
		STATE.rightTrigger = normalize(SDLGamepad.SDL_GetGamepadAxis(gamepad, SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER));
		STATE.jump = SDLGamepad.SDL_GetGamepadButton(gamepad, SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH);
		STATE.sneak = SDLGamepad.SDL_GetGamepadButton(gamepad, SDLGamepad.SDL_GAMEPAD_BUTTON_EAST);
		STATE.sprint = SDLGamepad.SDL_GetGamepadButton(gamepad, SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK);
		STATE.dpadUp = SDLGamepad.SDL_GetGamepadButton(gamepad, SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
		STATE.dpadDown = SDLGamepad.SDL_GetGamepadButton(gamepad, SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN);

		if (STATE.gyroAvailable) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer values = stack.mallocFloat(3);
				if (SDLGamepad.SDL_GetGamepadSensorData(gamepad, SDLSensor.SDL_SENSOR_GYRO, values)) {
					STATE.gyroX = values.get(0);
					STATE.gyroY = values.get(1);
					STATE.gyroZ = values.get(2);
				}
			}
		}
		if (STATE.accelAvailable) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer values = stack.mallocFloat(3);
				if (SDLGamepad.SDL_GetGamepadSensorData(gamepad, SDLSensor.SDL_SENSOR_ACCEL, values)) {
					STATE.accelX = values.get(0);
					STATE.accelY = values.get(1);
					STATE.accelZ = values.get(2);
				}
			}
		}
	}

	private static boolean isGameplayActive(Minecraft minecraft) {
		return isConnected() && minecraft.player != null && minecraft.screen == null
				&& minecraft.focused && Display.isActive();
	}

	public static boolean isBindingHeld(KeyBinding binding) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!isGameplayActive(minecraft)) return false;
		var options = minecraft.options;
		return (binding == options.jumpKey && STATE.jump)
				|| (binding == options.sneakKey && STATE.sneak)
				|| (binding == options.sprintKey && STATE.sprint)
				|| (binding == options.playerListKey && isActionHeld(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT))
				|| (binding == options.attackKey && ATTACK.held)
				|| (binding == options.useKey && USE.held);
	}

	public static boolean consumeBindingClick(KeyBinding binding) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!sampledInGameplay || !isGameplayActive(minecraft)) return false;
		var options = minecraft.options;
		return (binding == options.attackKey && ATTACK.consumeClick())
				|| (binding == options.useKey && USE.consumeClick())
				|| (binding == options.inventoryKey && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH))
				|| (binding == options.dropKey && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST))
				|| (binding == options.chatKey && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP))
				|| (binding == options.pickItemKey && consumeActionPress(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK));
	}

	private static boolean isActionHeld(int button) {
		return (actionButtonsDown & (1 << button)) != 0;
	}

	private static float normalize(short value) {
		return value < 0 ? value / 32768.0F : value / 32767.0F;
	}

	private static float applyDeadzone(float value) {
		float magnitude = Math.abs(value);
		if (magnitude <= DEADZONE) return 0.0F;
		float scaled = (magnitude - DEADZONE) / (1.0F - DEADZONE);
		return Math.copySign((float) Math.pow(scaled, 1.15), value);
	}

	public static String getGamepadName() {
		return gamepadName;
	}

	public static ControllerState getState() {
		return STATE;
	}

	public static boolean isConnected() {
		return gamepad != 0L;
	}

	public static boolean backPressed() {
		return STATE.sneak && !previousBack;
	}

	public static void combineAnalogMovement(net.minecraft.client.entity.living.player.Input input) {
		if (!isGameplayActive(Minecraft.getInstance())) return;
		float sideways = applyDeadzone(STATE.leftX);
		float forward = applyDeadzone(STATE.leftY);
		if (sideways == 0.0F && forward == 0.0F) return;

		float maxSpeed = input.sneaking ? 0.3F : 1.0F;
		// Minecraft's positive sideways input means left, while SDL's positive X means right.
		input.movementSideways -= sideways * maxSpeed;
		input.movementForward -= forward * maxSpeed;
		float magnitudeSquared = input.movementSideways * input.movementSideways
				+ input.movementForward * input.movementForward;
		if (magnitudeSquared > maxSpeed * maxSpeed) {
			float scale = maxSpeed / (float) Math.sqrt(magnitudeSquared);
			input.movementSideways *= scale;
			input.movementForward *= scale;
		}
	}

	public static boolean rumble(float lowFrequency, float highFrequency, int durationMillis) {
		return gamepad != 0L && SDLGamepad.SDL_RumbleGamepad(
				gamepad, toUnsignedShort(lowFrequency), toUnsignedShort(highFrequency), Math.max(0, durationMillis));
	}

	public static boolean rumbleTriggers(float left, float right, int durationMillis) {
		return gamepad != 0L && SDLGamepad.SDL_RumbleGamepadTriggers(
				gamepad, toUnsignedShort(left), toUnsignedShort(right), Math.max(0, durationMillis));
	}

	private static short toUnsignedShort(float value) {
		int scaled = Math.round(Math.max(0.0F, Math.min(1.0F, value)) * 65535.0F);
		return (short) scaled;
	}

	private static final class TriggerInput {
		private boolean down;
		private boolean held;
		private boolean clicked;

		private void update(boolean down, boolean gameplayActive) {
			held = gameplayActive && down && (held || !this.down);
			clicked = held && !this.down;
			this.down = down;
		}

		private boolean consumeClick() {
			boolean result = clicked;
			clicked = false;
			return result;
		}
	}

	public static final class ControllerState {
		public float leftX;
		public float leftY;
		public float rightX;
		public float rightY;
		public float leftTrigger;
		public float rightTrigger;
		public boolean jump;
		public boolean sneak;
		public boolean sprint;
		public boolean dpadUp;
		public boolean dpadDown;
		public boolean gyroAvailable;
		public float gyroX;
		public float gyroY;
		public float gyroZ;
		public boolean accelAvailable;
		public float accelX;
		public float accelY;
		public float accelZ;

		private void clear() {
			leftX = leftY = rightX = rightY = leftTrigger = rightTrigger = 0.0F;
			jump = sneak = sprint = dpadUp = dpadDown = gyroAvailable = accelAvailable = false;
			gyroX = gyroY = gyroZ = 0.0F;
			accelX = accelY = accelZ = 0.0F;
		}
	}
}
