package dev.isxander.controlify.legacy;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.resource.Identifier;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLIOStream;
import org.lwjgl.sdl.SDLStdinc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** SDL 3 gamepad input for Ornithe's LWJGL 3 runtime (provided by Pylon). */
public final class ControllerManager {
	private static final String SESSION_ONLY = "session";
	private static boolean initialized;
	private static boolean unavailable;
	private static long gamepad;
	private static String gamepadName;
	private static int gamepadId;
	private static String appliedPreference = "";
	private static int preferredInstanceId;
	private static boolean selectionRequested;
	private static SDL3GamepadDriver driver;
	private static boolean noGamepadLogged;
	private static int rescanTicks;
	private static int actionButtonsDown;
	private static int actionButtonsPressed;
	private static int blockedGameplayButtons;
	private static int blockedMenuButtons;
	private static int consumedActions;
	private static boolean sampledInGameplay;
	private static boolean sneaking;
	private static boolean sprinting;
	private static boolean wasFlying;
	private static boolean wasRiding;
	private static boolean playerListVisible;
	private static boolean dropClick;
	private static boolean pendingScreenshot;
	private static boolean gyroToggledOn;
	private static boolean controllerInput;
	private static boolean wasGivingInput;
	private static boolean menuStickMoving;
	private static float scrollAmount;
	private static LocalClientPlayerEntity previousPlayer;

	private static final ControllerStateImpl STATE = new ControllerStateImpl();
	private static final ControllerStateImpl FRAME_STATE = new ControllerStateImpl();
	private static final InGameInputHandler LOOK = new InGameInputHandler();
	private static final RumbleManager RUMBLE = new RumbleManager(state ->
			rumble(state.strong(), state.weak(), state.isZero() ? 0 : 100));
	private static final ControllerMenu MENU = new ControllerMenu();
	private static final TriggerInput ATTACK = new TriggerInput();
	private static final TriggerInput USE = new TriggerInput();
	private static final HoldRepeatHelper HOTBAR_PREVIOUS = new HoldRepeatHelper(10, 4);
	private static final HoldRepeatHelper HOTBAR_NEXT = new HoldRepeatHelper(10, 4);
	private static final HoldRepeatHelper DROP = new HoldRepeatHelper(20, 1);
	private static final HoldRepeatHelper SNAP = new HoldRepeatHelper(10, 3);
	private static final List<ControllerButton> CHAT_SHORTCUTS = List.of(
			ControllerButton.START, ControllerButton.X, ControllerButton.Y, ControllerButton.LEFT_STICK);
	private static int snapX;
	private static int snapY;

	private ControllerManager() {
	}

	public static void tick(Minecraft minecraft) {
		if (minecraft.screen == null) {
			MENU.releaseButton();
			scrollAmount = 0.0F;
			SNAP.reset();
			snapX = snapY = 0;
		}
		if (minecraft.player != previousPlayer) {
			previousPlayer = minecraft.player;
			sneaking = sprinting = wasFlying = wasRiding = playerListVisible = false;
		}
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
			loadGamepadMappings();
		}

		SDLGamepad.SDL_UpdateGamepads();
		String preference = preference();
		if (!appliedPreference.equals(preference)) {
			if (!selectionRequested) preferredInstanceId = 0;
			appliedPreference = preference;
			selectionRequested = true;
		}
		if (selectionRequested || (gamepad != 0L && !SDLGamepad.SDL_GamepadConnected(gamepad))) {
			closeGamepad();
			if (minecraft.screen instanceof RadialMenuScreen) minecraft.openScreen(null);
			rescanTicks = 0;
			selectionRequested = false;
		}
		if (gamepad == 0L) {
			if (rescanTicks-- <= 0) {
				openSelectedGamepad(preference);
				rescanTicks = 20;
			}
		}

		if (gamepad == 0L) {
			return;
		}

		driver.updateInput(STATE);
		if (Display.isActive()) driver.updateGyroCalibration();
		if (isGameplayActive(minecraft) && LegacyConfig.vibration) rumbleUsingItem(minecraft.player);
		RUMBLE.setSilent(!Display.isActive());
		RUMBLE.tick();
		sampledInGameplay = isGameplayActive(minecraft);
		int buttons = readActionButtons();
		actionButtonsPressed = buttons & ~actionButtonsDown;
		actionButtonsDown = buttons;
		boolean givingInput = STATE.isGivingInput();
		if (Display.isActive() && (actionButtonsPressed != 0 || (givingInput && !wasGivingInput))) {
			controllerInput = true;
		}
		wasGivingInput = givingInput;
		blockedGameplayButtons = sampledInGameplay ? blockedGameplayButtons & buttons : buttons;
		blockedMenuButtons = minecraft.screen == null || !Display.isActive() ? buttons : blockedMenuButtons & buttons;
		consumedActions = 0;
		ATTACK.update(isGameplayActionHeld(ControllerAction.ATTACK), sampledInGameplay);
		USE.update(isGameplayActionHeld(ControllerAction.USE), sampledInGameplay);
		if (minecraft.screen instanceof RadialMenuScreen radial) {
			tickRadial(radial);
		} else if (sampledInGameplay) {
			if (LegacyConfig.gyroButtonMode == GyroButtonMode.TOGGLE && consumeActionPress(ControllerAction.GYRO)) {
				gyroToggledOn = !gyroToggledOn;
			}
			updateMovementToggles(minecraft.player);
			handleGameplayButtons(minecraft);
		}
	}

	public static void updateFrame(Minecraft minecraft) {
		boolean gameplay = isGameplayActive(minecraft);
		boolean menu = minecraft.screen != null && !(minecraft.screen instanceof RadialMenuScreen) && canNavigateMenu();
		if (!gameplay) LOOK.reset();
		if (!menu) {
			MENU.resetMotion();
			menuStickMoving = false;
		}
		if (!gameplay && !menu) return;

		SDLGamepad.SDL_UpdateGamepads();
		if (!SDLGamepad.SDL_GamepadConnected(gamepad)) {
			LOOK.reset();
			MENU.resetMotion();
			return;
		}
		driver.updateAxes(FRAME_STATE);
		if (menu) {
			float deadzone = LegacyConfig.leftStickDeadzone();
			float x = readAxis(FRAME_STATE, GamepadInputs.LEFT_STICK_AXIS_RIGHT, GamepadInputs.LEFT_STICK_AXIS_LEFT, deadzone);
			float y = readAxis(FRAME_STATE, GamepadInputs.LEFT_STICK_AXIS_DOWN, GamepadInputs.LEFT_STICK_AXIS_UP, deadzone);
			MENU.moveCursor(x, y);
			boolean moving = x != 0.0F || y != 0.0F;
			if (menuStickMoving && !moving && !MENU.isDragging() && minecraft.screen instanceof InventoryMenuScreen inventory) {
				InventoryNavigation.snapToClosest(inventory, MENU);
			}
			menuStickMoving = moving;
			return;
		}

		float deadzone = LegacyConfig.rightStickDeadzone();
		float x = readAxis(FRAME_STATE, GamepadInputs.RIGHT_STICK_AXIS_RIGHT, GamepadInputs.RIGHT_STICK_AXIS_LEFT, deadzone);
		float y = readAxis(FRAME_STATE, GamepadInputs.RIGHT_STICK_AXIS_DOWN, GamepadInputs.RIGHT_STICK_AXIS_UP, deadzone);
		ItemStack itemInUse = minecraft.player.getItemInUse();
		boolean aiming = itemInUse != null && itemInUse.getUseAction() == UseAction.BOW;
		LOOK.handleRegularLook(x, y, LegacyConfig.cameraSpeedMultiplier(), LegacyConfig.verticalCameraSpeedMultiplier(),
				LegacyConfig.lookCurve, aiming && LegacyConfig.reduceBowSensitivity);
		float gyroSensitivity = LegacyConfig.gyroAimMultiplier();
		if (gyroSensitivity > 0.0F && shouldUseGyro(aiming)) {
			LOOK.handleGyroLook(driver.updateGyro(), gyroSensitivity, LegacyConfig.gyroYawMode,
					LegacyConfig.invertGyroPitch, LegacyConfig.invertGyroYaw);
		}

		LOOK.processPlayerLook(minecraft.player, System.nanoTime());
	}

	private static boolean shouldUseGyro(boolean aiming) {
		return aiming || switch (LegacyConfig.gyroButtonMode) {
			case ON -> isGameplayActionHeld(ControllerAction.GYRO);
			case INVERT -> !isGameplayActionHeld(ControllerAction.GYRO);
			case TOGGLE -> gyroToggledOn;
			case OFF -> true;
		};
	}

	private static float readAxis(ControllerStateImpl state, Identifier positive, Identifier negative, float deadzone) {
		return ControllerUtils.deadzone(state.getAxisState(positive), deadzone)
				- ControllerUtils.deadzone(state.getAxisState(negative), deadzone);
	}

	public static boolean canNavigateMenu() {
		return isConnected() && Display.isActive() && Mouse.isCreated() && !Mouse.isGrabbed();
	}

	public static void handleMenuInput(Screen screen, List<ButtonWidget> buttons) {
		if (sampledInGameplay) return;
		if (screen instanceof ControllerChatScreen chat) {
			for (ControllerButton button : CHAT_SHORTCUTS) {
				if (wasPressed(button) && chat.shortcut(button)) return;
			}
		}
		boolean leftClick = consumeActionPress(ControllerAction.MENU_SELECT);
		boolean rightClick = consumeActionPress(ControllerAction.MENU_RIGHT_CLICK);
		boolean scrollUp = consumeActionPress(ControllerAction.MENU_SCROLL_UP);
		boolean scrollDown = consumeActionPress(ControllerAction.MENU_SCROLL_DOWN);
		int dy = Boolean.compare(isMenuButtonHeld(ControllerButton.DPAD_DOWN), isMenuButtonHeld(ControllerButton.DPAD_UP));
		int dx = dy == 0
				? Boolean.compare(isMenuButtonHeld(ControllerButton.DPAD_RIGHT), isMenuButtonHeld(ControllerButton.DPAD_LEFT))
				: 0;
		if (dx != snapX || dy != snapY) SNAP.reset();
		snapX = dx;
		snapY = dy;
		if (SNAP.shouldAction(dx != 0 || dy != 0)) {
			if (screen instanceof InventoryMenuScreen inventory) InventoryNavigation.snap(inventory, MENU, dx, dy);
			else MENU.snapToButton(screen, buttons, dx, dy);
		}
		if (screen instanceof InventoryMenuScreen inventory && consumeActionPress(ControllerAction.QUICK_MOVE)
				&& !MENU.isDragging() && InventoryNavigation.quickMove(inventory)) {
			leftClick = rightClick = false;
		}
		scrollAmount += readAxis(STATE, GamepadInputs.RIGHT_STICK_AXIS_UP, GamepadInputs.RIGHT_STICK_AXIS_DOWN,
				LegacyConfig.rightStickDeadzone());
		int scroll = (int) scrollAmount;
		scrollAmount -= scroll;
		int held = actionButtonsDown & ~blockedMenuButtons;
		MENU.handleInput(screen, leftClick, (held & ControllerAction.MENU_SELECT.buttonMask()) != 0, rightClick,
				(held & ControllerAction.MENU_RIGHT_CLICK.buttonMask()) != 0, scroll + Boolean.compare(scrollUp, scrollDown));
	}

	private static boolean isMenuButtonHeld(ControllerButton button) {
		return (actionButtonsDown & ~blockedMenuButtons & button.mask) != 0;
	}

	public static void releaseMenuButton() {
		MENU.releaseButton();
	}

	private static int readActionButtons() {
		int buttons = 0;
		for (ControllerButton button : ControllerButton.DIGITAL_BUTTONS) {
			if (STATE.isButtonDown(button.input)) {
				buttons |= button.mask;
			}
		}
		if (STATE.getAxisState(GamepadInputs.LEFT_TRIGGER_AXIS) >= LegacyConfig.activationThreshold()) buttons |= ControllerButton.LEFT_TRIGGER.mask;
		if (STATE.getAxisState(GamepadInputs.RIGHT_TRIGGER_AXIS) >= LegacyConfig.activationThreshold()) buttons |= ControllerButton.RIGHT_TRIGGER.mask;
		return buttons;
	}

	private static boolean wasPressed(ControllerButton button) {
		return (actionButtonsPressed & button.mask) != 0;
	}

	private static boolean consumeActionPress(ControllerAction action) {
		int mask = 1 << action.ordinal();
		int eligibleButtons = sampledInGameplay ? actionButtonsPressed & ~blockedGameplayButtons : actionButtonsPressed;
		boolean pressed = (eligibleButtons & action.buttonMask()) != 0 && (consumedActions & mask) == 0;
		consumedActions |= mask;
		return pressed;
	}

	public static boolean consumePausePress() {
		return consumeActionPress(ControllerAction.PAUSE);
	}

	private static void handleGameplayButtons(Minecraft minecraft) {
		if (consumePausePress()) {
			minecraft.pauseGame();
			actionButtonsPressed = 0;
			return;
		}

		if (consumeActionPress(ControllerAction.RADIAL_MENU)) {
			minecraft.openScreen(new RadialMenuScreen());
			return;
		}
		if (consumeActionPress(ControllerAction.CHAT)) {
			openChat(minecraft);
			return;
		}
		dropClick = DROP.shouldAction(isGameplayActionHeld(ControllerAction.DROP));
		if (consumeActionPress(ControllerAction.PLAYER_LIST)) playerListVisible = !playerListVisible;

		boolean previousSlot = HOTBAR_PREVIOUS.shouldAction(isGameplayActionHeld(ControllerAction.HOTBAR_PREV));
		boolean nextSlot = HOTBAR_NEXT.shouldAction(isGameplayActionHeld(ControllerAction.HOTBAR_NEXT));
		int scroll = Boolean.compare(previousSlot, nextSlot);
		if (minecraft.player.isSpectator()) {
			var spectatorGui = minecraft.gui.getSpectatorGui();
			if (consumeActionPress(ControllerAction.PICK_BLOCK)) {
				spectatorGui.mouseMiddleClicked();
			}
			if (scroll != 0 && spectatorGui.isMenuActive()) spectatorGui.mouseScrolled(-scroll);
		} else if (scroll != 0) {
			minecraft.player.inventory.scrollInHotbar(scroll);
		}

		if (consumeActionPress(ControllerAction.PERSPECTIVE)) changePerspective(minecraft);
	}

	private static void updateMovementToggles(LocalClientPlayerEntity player) {
		boolean flying = player.abilities.flying;
		boolean riding = player.isRiding();
		if (!LegacyConfig.toggleSneak || flying || riding || (player.isInWater() && !player.onGround)) {
			sneaking = isGameplayActionHeld(ControllerAction.SNEAK);
		} else if (consumeActionPress(ControllerAction.SNEAK)) {
			sneaking = !sneaking;
		}
		if ((!flying && wasFlying && player.onGround) || (!riding && wasRiding)) sneaking = false;
		if (!LegacyConfig.toggleSprint) {
			sprinting = isGameplayActionHeld(ControllerAction.SPRINT);
		} else if (consumeActionPress(ControllerAction.SPRINT)) {
			sprinting = !sprinting;
		}
		wasFlying = flying;
		wasRiding = riding;
	}

	private static void tickRadial(RadialMenuScreen radial) {
		boolean focused = Display.isActive();
		boolean cancel = !focused || consumeActionPress(ControllerAction.MENU_BACK);
		int page = Boolean.compare(wasPressed(ControllerButton.RIGHT_BUMPER), wasPressed(ControllerButton.LEFT_BUMPER));
		float deadzone = LegacyConfig.rightStickDeadzone();
		float x = readAxis(STATE, GamepadInputs.RIGHT_STICK_AXIS_RIGHT, GamepadInputs.RIGHT_STICK_AXIS_LEFT, deadzone);
		float y = readAxis(STATE, GamepadInputs.RIGHT_STICK_AXIS_DOWN, GamepadInputs.RIGHT_STICK_AXIS_UP, deadzone);
		radial.onControllerInput(isActionHeld(ControllerAction.RADIAL_MENU), cancel, page, x, y);
	}

	static void invokeRadialAction(Minecraft minecraft, RadialMenuScreen.Action action) {
		if (minecraft.player == null) return;
		switch (action) {
			case HUD -> minecraft.options.hideGui = !minecraft.options.hideGui;
			case PERSPECTIVE -> changePerspective(minecraft);
			case DROP_STACK -> {
				if (!minecraft.player.isSpectator()) minecraft.player.dropItem(true);
			}
			case CHAT -> openChat(minecraft);
			case PICK_BLOCK -> KeyBinding.click(minecraft.options.pickItemKey.getKeyCode());
			case SCREENSHOT -> pendingScreenshot = true;
			case PLAYER_LIST -> playerListVisible = !playerListVisible;
			case DEBUG -> minecraft.options.debugEnabled = !minecraft.options.debugEnabled;
			case PROFILER -> minecraft.options.debugProfilerEnabled = !minecraft.options.debugProfilerEnabled;
			case TPS -> minecraft.options.debugTpsEnabled = !minecraft.options.debugTpsEnabled;
			case RELOAD_CHUNKS -> minecraft.worldRenderer.reload();
			case TOOLTIPS -> {
				minecraft.options.advancedItemTooltips = !minecraft.options.advancedItemTooltips;
				minecraft.options.save();
			}
			case RELOAD_RESOURCES -> minecraft.reloadResources();
			case CLEAR_CHAT -> minecraft.gui.getChat().clear();
		}
	}

	public static void afterRender(Minecraft minecraft) {
		if (!pendingScreenshot) return;
		pendingScreenshot = false;
		minecraft.gui.getChat().addMessage(Screenshot.take(minecraft.gameDir, minecraft.width, minecraft.height,
				minecraft.getRenderTarget()));
	}

	private static void openChat(Minecraft minecraft) {
		if (minecraft.options.chatVisibility == PlayerEntity.ChatVisibility.HIDDEN) return;
		minecraft.openScreen(LegacyConfig.onScreenKeyboard ? new ControllerChatScreen() : new ChatScreen());
	}

	private static void changePerspective(Minecraft minecraft) {
		int perspective = (minecraft.options.perspective + 1) % 3;
		minecraft.options.perspective = perspective;
		if (perspective == 0) minecraft.gameRenderer.updateShader(minecraft.getCamera());
		if (perspective == 1) minecraft.gameRenderer.updateShader(null);
		minecraft.worldRenderer.onViewChanged();
	}

	record GamepadDevice(int id, String name, String key) {
	}

	private static String preference() {
		return Objects.requireNonNullElse(LegacyConfig.preferredController, "");
	}

	static boolean isAutomaticSelection() {
		return preference().isEmpty();
	}

	static int activeGamepadId() {
		return gamepadId;
	}

	static void selectController(GamepadDevice device, List<GamepadDevice> devices) {
		if (device == null) {
			LegacyConfig.preferredController = "";
			preferredInstanceId = 0;
		} else {
			// A session ID distinguishes controllers that SDL gives the same persistent identity.
			boolean ambiguous = devices.stream().anyMatch(other -> other.id != device.id && other.key.equals(device.key));
			LegacyConfig.preferredController = ambiguous ? SESSION_ONLY : device.key;
			preferredInstanceId = device.id;
		}
		selectionRequested = true;
		LegacyConfig.INSTANCE.save();
	}

	static List<GamepadDevice> connectedGamepads() {
		if (!initialized || unavailable) return List.of();
		IntBuffer gamepads = SDLGamepad.SDL_GetGamepads();
		if (gamepads == null) return List.of();
		try {
			List<GamepadDevice> devices = new ArrayList<>(gamepads.remaining());
			for (int index = gamepads.position(); index < gamepads.limit(); index++) {
				int id = gamepads.get(index);
				String name = SDLGamepad.SDL_GetGamepadNameForID(id);
				String path = SDLGamepad.SDL_GetGamepadPathForID(id);
				String key = path != null && !path.isBlank() ? "path:" + path
						: name != null && !name.isBlank() ? "name:" + name : SESSION_ONLY;
				devices.add(new GamepadDevice(id, Objects.requireNonNullElse(name, "Controller " + id), key));
			}
			return devices;
		} finally {
			SDLStdinc.SDL_free(gamepads);
		}
	}

	private static void openSelectedGamepad(String preference) {
		List<GamepadDevice> devices = connectedGamepads();
		if (devices.isEmpty()) logNoGamepadOnce();
		if (preference.isEmpty()) {
			for (GamepadDevice device : devices) {
				if (openGamepad(device)) return;
			}
			return;
		}

		GamepadDevice uniqueMatch = null;
		boolean ambiguous = false;
		for (GamepadDevice device : devices) {
			if (device.id == preferredInstanceId && (preference.equals(SESSION_ONLY) || preference.equals(device.key))) {
				openGamepad(device);
				return;
			}
			if (!preference.equals(SESSION_ONLY) && preference.equals(device.key)) {
				ambiguous |= uniqueMatch != null;
				uniqueMatch = device;
			}
		}
		if (uniqueMatch != null && !ambiguous && openGamepad(uniqueMatch)) preferredInstanceId = uniqueMatch.id;
	}

	private static boolean openGamepad(GamepadDevice device) {
		long candidate = SDLGamepad.SDL_OpenGamepad(device.id);
		if (candidate == 0L) return false;
		gamepad = candidate;
		gamepadId = device.id;
		gamepadName = device.name;
		noGamepadLogged = false;
		String serial = SDLGamepad.SDL_GetGamepadSerial(gamepad);
		String hardware = Short.toUnsignedInt(SDLGamepad.SDL_GetGamepadVendor(gamepad)) + ":"
				+ Short.toUnsignedInt(SDLGamepad.SDL_GetGamepadProduct(gamepad));
		String profileKey = serial != null && !serial.isBlank() ? hardware + ":serial:" + serial
				: hardware + ":" + device.key;
		ControllerProfiles.activate(profileKey);
		driver = new SDL3GamepadDriver(gamepad);
		driver.updateInput(STATE);
		actionButtonsDown = readActionButtons();
		suspendGameplayButtons();
		ControlifyLegacy.LOGGER.info("Active controller: {}", gamepadName);
		return true;
	}

	private static void closeGamepad() {
		MENU.releaseButton();
		RUMBLE.clearEffects();
		RUMBLE.tick();
		if (gamepad != 0L) {
			ControllerProfiles.save();
			SDLGamepad.SDL_CloseGamepad(gamepad);
		}
		gamepad = 0L;
		gamepadId = 0;
		gamepadName = null;
		driver = null;
		STATE.clearState();
		FRAME_STATE.clearState();
		actionButtonsDown = actionButtonsPressed = blockedGameplayButtons = 0;
		sneaking = sprinting = playerListVisible = false;
		controllerInput = wasGivingInput = gyroToggledOn = false;
		suspendGameplayButtons();
		LOOK.reset();
		MENU.resetMotion();
	}

	public static void shutdown() {
		if (gamepad != 0L) closeGamepad();
		else ControllerProfiles.save();
	}

	public static void suspendGameplayButtons() {
		blockedGameplayButtons |= actionButtonsDown;
		blockedMenuButtons |= actionButtonsDown;
		HOTBAR_PREVIOUS.reset();
		HOTBAR_NEXT.reset();
		DROP.reset();
		dropClick = false;
		ATTACK.update(false, false);
		USE.update(false, false);
	}

	private static void logNoGamepadOnce() {
		if (!noGamepadLogged) {
			ControlifyLegacy.LOGGER.info("No SDL gamepads connected");
			noGamepadLogged = true;
		}
	}

	// Adapted from upstream SDLControllerManager#loadGamepadMappings for LWJGL and the standalone JAR.
	private static void loadGamepadMappings() {
		String path = "/assets/controlify/controllers/gamecontrollerdb-sdl3.txt";
		try (InputStream input = ControllerManager.class.getResourceAsStream(path)) {
			if (input == null) {
				ControlifyLegacy.LOGGER.error("Could not find controller database: {}", path);
				return;
			}
			byte[] bytes = input.readAllBytes();
			ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
			long stream = SDLIOStream.SDL_IOFromConstMem(buffer);
			if (stream == 0L) {
				ControlifyLegacy.LOGGER.error("Could not open controller database: {}", SDLError.SDL_GetError());
				return;
			}
			int count;
			try {
				count = SDLGamepad.SDL_AddGamepadMappingsFromIO(stream, true);
			} finally {
				java.lang.ref.Reference.reachabilityFence(buffer);
			}
			if (count < 0) {
				ControlifyLegacy.LOGGER.error("Could not load controller mappings: {}", SDLError.SDL_GetError());
			} else {
				ControlifyLegacy.LOGGER.info("Loaded {} SDL gamepad mappings", count);
			}
		} catch (IOException e) {
			ControlifyLegacy.LOGGER.error("Could not read controller database", e);
		}
	}

	private static boolean isGameplayActive(Minecraft minecraft) {
		return isConnected() && minecraft.player != null && minecraft.screen == null
				&& minecraft.focused && Display.isActive();
	}

	public static boolean isBindingHeld(KeyBinding binding) {
		Minecraft minecraft = Minecraft.getInstance();
		var options = minecraft.options;
		if (binding == options.playerListKey && playerListVisible && minecraft.player != null
				&& minecraft.screen == null && minecraft.focused && Display.isActive()) return true;
		if (!sampledInGameplay || !isGameplayActive(minecraft)) return false;
		return (binding == options.jumpKey && isGameplayActionHeld(ControllerAction.JUMP))
				|| (binding == options.sneakKey && sneaking)
				|| (binding == options.sprintKey && sprinting)
				|| (binding == options.attackKey && ATTACK.held)
				|| (binding == options.useKey && USE.held);
	}

	public static boolean consumeBindingClick(KeyBinding binding) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!sampledInGameplay || !isGameplayActive(minecraft)) return false;
		var options = minecraft.options;
		if (binding == options.dropKey && dropClick) {
			dropClick = false;
			return true;
		}
		return (binding == options.attackKey && ATTACK.consumeClick())
				|| (binding == options.useKey && USE.consumeClick())
				|| (binding == options.inventoryKey && consumeActionPress(ControllerAction.INVENTORY))
				|| (binding == options.pickItemKey && consumeActionPress(ControllerAction.PICK_BLOCK));
	}

	private static boolean isActionHeld(ControllerAction action) {
		return (actionButtonsDown & action.buttonMask()) != 0;
	}

	private static boolean isGameplayActionHeld(ControllerAction action) {
		return (actionButtonsDown & ~blockedGameplayButtons & action.buttonMask()) != 0;
	}

	public static String getGamepadName() {
		return gamepadName;
	}

	public static boolean isConnected() {
		return gamepad != 0L;
	}

	public static boolean isControllerInput() {
		return isConnected() && controllerInput;
	}

	public static void keyboardMouseUsed() {
		controllerInput = false;
	}

	public static boolean backPressed() {
		return !sampledInGameplay && consumeActionPress(ControllerAction.MENU_BACK);
	}

	public static void combineAnalogMovement(net.minecraft.client.entity.living.player.Input input) {
		if (!isGameplayActive(Minecraft.getInstance())) return;
		float deadzone = LegacyConfig.leftStickDeadzone();
		float left = readAxis(STATE, GamepadInputs.LEFT_STICK_AXIS_LEFT, GamepadInputs.LEFT_STICK_AXIS_RIGHT, deadzone);
		float forward = readAxis(STATE, GamepadInputs.LEFT_STICK_AXIS_UP, GamepadInputs.LEFT_STICK_AXIS_DOWN, deadzone);
		ControllerPlayerMovement.combineWithKeyboard(input, forward, left);
	}

	public static boolean rumble(float lowFrequency, float highFrequency, int durationMillis) {
		return gamepad != 0L && SDLGamepad.SDL_RumbleGamepad(
				gamepad, toUnsignedShort(lowFrequency), toUnsignedShort(highFrequency), Math.max(0, durationMillis));
	}

	public static void rumbleOnDamage(float damage) {
		if (damage <= 0 || !LegacyConfig.vibration || !isConnected() || !Display.isActive()) return;
		// Match the incoming-damage effect used by upstream's damage packet hook.
		RUMBLE.play(BasicRumbleEffect.constant(0.8F, 0.5F, 5));
	}

	// The upstream use-item effect evaluates these strengths against the item's use time.
	private static void rumbleUsingItem(LocalClientPlayerEntity player) {
		ItemStack stack = player.getItemInUse();
		if (stack == null) return;
		int ticks = player.getRemainingItemUseDuration();
		switch (stack.getUseAction()) {
			case BOW -> {
				float draw = ticks / 20.0F;
				float power = Math.clamp((draw * draw + 2.0F * draw) / 3.0F, 0.0F, 1.0F);
				RUMBLE.play(BasicRumbleEffect.constant(ticks > 20 && ticks % 7 <= 3 ? 0.1F : 0.0F, power, 1));
			}
			case BLOCK -> RUMBLE.play(BasicRumbleEffect.constant(0.0F, ticks % 4 / 4.0F * 0.12F + 0.05F, 1));
			case EAT, DRINK -> RUMBLE.play(BasicRumbleEffect.constant(0.1F, 0.2F, 1));
			default -> { }
		}
	}

	public static void rumbleOnExplosion(double distance, float radius) {
		if (!LegacyConfig.vibration || !isConnected() || !Display.isActive() || !Double.isFinite(distance)
				|| !Float.isFinite(radius) || radius <= 0) return;
		// Upstream measures falloff from the blast's surface and scales by its radius.
		float proximity = 1.0F - Math.max((float) distance - radius, 0.0F) / 32.0F;
		if (proximity <= 0.0F) return;
		float magnitude = proximity * proximity * proximity * Math.min((float) Math.sqrt(radius / 6.0F), 1.0F);
		RUMBLE.play(BasicRumbleEffect.seq(
				BasicRumbleEffect.constant(magnitude, magnitude, 2),
				BasicRumbleEffect.byTime(t -> {
					float decay = (1 - t) * (1 - t);
					return new RumbleState(magnitude * 0.8F * decay, magnitude * 0.2F * decay);
				}, 8)));
	}

	static void testRumble() {
		if (isConnected()) RUMBLE.play(BasicRumbleEffect.constant(0.65F, 0.4F, 5));
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
}
