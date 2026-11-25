/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.ingame;

import dev.isxander.controlify.controller.gyro.GyroYawMode;
import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver.GyroState;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;

/** The regular-stick and absolute-gyro paths from upstream's InGameInputHandler. */
public final class InGameInputHandler {
	private static final double DEGREES_PER_TICK = 10.0;
	private static final double VANILLA_DEGREES_PER_UNIT = 0.15;
	private static final double NANOS_PER_TICK = 50_000_000.0;

	private long lastFrameNanos;
	private double lookInputX;
	private double lookInputY;

	public void handleRegularLook(float x, float y, float horizontalSensitivity, float verticalSensitivity,
			InputCurve curve, boolean aiming) {
		// Apply the curve to the vector length to keep the angle of the input.
		double length = Math.hypot(x, y);
		double scale = length == 0.0 ? 0.0 : curve.apply(Math.min(length, 1.0)) / length;
		double speed = DEGREES_PER_TICK * (aiming ? 0.6F : 1.0F);
		lookInputX = x * scale * speed * horizontalSensitivity;
		lookInputY = y * scale * speed * verticalSensitivity;
	}

	public void handleGyroLook(GyroState gyro, float sensitivity, GyroYawMode yawMode, boolean invertPitch, boolean invertYaw) {
		// SDL reports radians per second. Controlify's look impulse is in degrees per tick.
		float yaw = switch (yawMode) {
			case YAW -> finiteRate(gyro.yaw());
			case ROLL -> finiteRate(gyro.roll());
			case BOTH -> finiteRate(gyro.yaw()) + finiteRate(gyro.roll());
		};
		lookInputX -= Math.toDegrees(yaw) / 20.0 * sensitivity * (invertYaw ? -1 : 1);
		lookInputY -= Math.toDegrees(finiteRate(gyro.pitch())) / 20.0 * sensitivity * (invertPitch ? -1 : 1);
	}

	public void processPlayerLook(LocalClientPlayerEntity player, long now) {
		double deltaTime = lastFrameNanos == 0L ? 0.0 : Math.min((now - lastFrameNanos) / NANOS_PER_TICK, 1.0);
		lastFrameNanos = now;
		double velX = lookInputX / VANILLA_DEGREES_PER_UNIT * deltaTime;
		double velY = lookInputY / VANILLA_DEGREES_PER_UNIT * deltaTime;
		if (velX != 0.0 || velY != 0.0) {
			// Feather's camera method subtracts the vertical delta.
			player.updateLocalPlayerCamera((float) velX, (float) -velY);
		}
	}

	private static float finiteRate(float rate) {
		return Float.isFinite(rate) ? rate : 0.0F;
	}

	public void reset() {
		lastFrameNanos = 0L;
		lookInputX = lookInputY = 0.0;
	}
}
