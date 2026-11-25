/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroCalibrator;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.sdl.SDLGamepad.*;

public final class SDL3GamepadDriver {
	private static final int GYRO_AXIS_COUNT = 3;
	private static final List<SDLGamepadInputMapping> INPUT_MAPPINGS = List.of(
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_RIGHT, SDL_GAMEPAD_AXIS_LEFTX, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_LEFT, SDL_GAMEPAD_AXIS_LEFTX, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_DOWN, SDL_GAMEPAD_AXIS_LEFTY, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_UP, SDL_GAMEPAD_AXIS_LEFTY, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, SDL_GAMEPAD_AXIS_RIGHTX, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_LEFT, SDL_GAMEPAD_AXIS_RIGHTX, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_DOWN, SDL_GAMEPAD_AXIS_RIGHTY, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_UP, SDL_GAMEPAD_AXIS_RIGHTY, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_TRIGGER_AXIS, SDL_GAMEPAD_AXIS_LEFT_TRIGGER, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_TRIGGER_AXIS, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER, true),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.SOUTH_BUTTON, SDL_GAMEPAD_BUTTON_SOUTH),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.EAST_BUTTON, SDL_GAMEPAD_BUTTON_EAST),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.WEST_BUTTON, SDL_GAMEPAD_BUTTON_WEST),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.NORTH_BUTTON, SDL_GAMEPAD_BUTTON_NORTH),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_SHOULDER_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_SHOULDER_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.BACK_BUTTON, SDL_GAMEPAD_BUTTON_BACK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.START_BUTTON, SDL_GAMEPAD_BUTTON_START),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.GUIDE_BUTTON, SDL_GAMEPAD_BUTTON_GUIDE),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_UP_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_UP),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_DOWN_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_DOWN),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_LEFT_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_LEFT),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_RIGHT_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_STICK_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_STICK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_STICK_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_STICK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_1_BUTTON, SDL_GAMEPAD_BUTTON_MISC1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_2_BUTTON, SDL_GAMEPAD_BUTTON_MISC2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_3_BUTTON, SDL_GAMEPAD_BUTTON_MISC3),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_4_BUTTON, SDL_GAMEPAD_BUTTON_MISC4),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_5_BUTTON, SDL_GAMEPAD_BUTTON_MISC5),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_6_BUTTON, SDL_GAMEPAD_BUTTON_MISC6),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_PADDLE_1_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_PADDLE1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_PADDLE_2_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_PADDLE2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_PADDLE_1_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_PADDLE_2_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.TOUCHPAD_1_BUTTON, SDL_GAMEPAD_BUTTON_TOUCHPAD)
	);

	private static final List<SDLGamepadInputMapping.AxisMapping> AXES = INPUT_MAPPINGS.stream()
			.filter(SDLGamepadInputMapping.AxisMapping.class::isInstance)
			.map(SDLGamepadInputMapping.AxisMapping.class::cast)
			.toList();

	private final long gamepad;
	private final int gyroSensor;
	private final GyroCalibrator gyroCalibrator = new GyroCalibrator();

	public SDL3GamepadDriver(long gamepad) {
		this.gamepad = gamepad;
		this.gyroSensor = enableGyro();
	}

	public void updateInput(ControllerStateImpl state) {
		state.clearState();
		for (SDLGamepadInputMapping mapping : INPUT_MAPPINGS) {
			mapping.applyState(gamepad, state);
		}
	}

	public void updateAxes(ControllerStateImpl state) {
		state.clearState();
		for (SDLGamepadInputMapping.AxisMapping mapping : AXES) {
			mapping.applyState(gamepad, state);
		}
	}

	public GyroState updateGyro() {
		GyroState raw = readGyro();
		return raw == null ? GyroState.ZERO : gyroCalibrator.applyCalibration(raw);
	}

	public void updateGyroCalibration() {
		GyroState raw = readGyro();
		if (raw != null) gyroCalibrator.update(raw);
	}

	private GyroState readGyro() {
		if (gyroSensor == SDLSensor.SDL_SENSOR_INVALID) return null;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer values = stack.mallocFloat(GYRO_AXIS_COUNT);
			if (SDLGamepad.SDL_GetGamepadSensorData(gamepad, gyroSensor, values)) {
				return new GyroState(values.get(0), values.get(1), values.get(2));
			}
		}
		return null;
	}

	private int enableGyro() {
		for (int sensor : new int[] {SDLSensor.SDL_SENSOR_GYRO, SDLSensor.SDL_SENSOR_GYRO_R, SDLSensor.SDL_SENSOR_GYRO_L}) {
			if (SDLGamepad.SDL_GamepadHasSensor(gamepad, sensor)
					&& SDLGamepad.SDL_SetGamepadSensorEnabled(gamepad, sensor, true)) {
				return sensor;
			}
		}
		return SDLSensor.SDL_SENSOR_INVALID;
	}

	public record GyroState(float pitch, float yaw, float roll) {
		private static final GyroState ZERO = new GyroState(0.0F, 0.0F, 0.0F);
	}
}
