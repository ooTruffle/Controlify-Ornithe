/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver.GyroState;

/**
 * Implements rolling gyro calibration using exponential moving average
 * for background bias estimation. The calibrator continuously updates
 * during stationary periods (low angular velocity) to track and correct
 * for gyro drift over time.
 */
public class GyroCalibrator {
	// Exponential moving average (EMA) alpha value - lower = slower adaptation, more stable
	// 0.001 means it takes ~1000 samples to substantially update the bias estimate
	private static final float EMA_ALPHA = 0.001f;

	// Threshold for detecting stationary state (rad/s)
	// If all axes are below this threshold, we consider the gyro stationary
	private static final float STATIONARY_THRESHOLD = 0.02f;

	// Minimum number of consecutive stationary samples before updating bias
	private static final int MIN_STATIONARY_SAMPLES = 10;

	// Current bias estimate (offset to subtract from raw readings)
	private GyroState biasEstimate = new GyroState(0, 0, 0);

	// Counter for consecutive stationary samples
	private int stationarySampleCount = 0;

	/**
	 * Update the calibration with a new gyro sample.
	 * This should be called every time new gyro data arrives.
	 *
	 * @param rawGyroState The raw gyro reading from SDL3
	 */
	public void update(GyroState rawGyroState) {
		// Check if the gyro appears to be stationary
		boolean isStationary = isStationary(rawGyroState);

		if (isStationary) {
			stationarySampleCount++;

			// Only update bias estimate if we've been stationary long enough
			if (stationarySampleCount >= MIN_STATIONARY_SAMPLES) {
				// Update bias estimate using exponential moving average
				// New bias = (1 - alpha) * old_bias + alpha * current_reading
				biasEstimate = new GyroState(
						(1 - EMA_ALPHA) * biasEstimate.pitch() + EMA_ALPHA * rawGyroState.pitch(),
						(1 - EMA_ALPHA) * biasEstimate.yaw() + EMA_ALPHA * rawGyroState.yaw(),
						(1 - EMA_ALPHA) * biasEstimate.roll() + EMA_ALPHA * rawGyroState.roll());
			}
		} else {
			// Reset stationary counter when motion is detected
			stationarySampleCount = 0;
		}
	}

	/**
	 * Apply the current bias correction to a raw gyro reading.
	 *
	 * @param rawGyroState The raw gyro reading from SDL3
	 * @return Calibrated gyro state with bias removed
	 */
	public GyroState applyCalibration(GyroState rawGyroState) {
		return new GyroState(
			rawGyroState.pitch() - biasEstimate.pitch(),
			rawGyroState.yaw() - biasEstimate.yaw(),
			rawGyroState.roll() - biasEstimate.roll()
		);
	}

	/**
	 * Check if the gyro appears to be stationary based on angular velocity.
	 */
	private boolean isStationary(GyroState state) {
		return Math.abs(state.pitch()) < STATIONARY_THRESHOLD
			&& Math.abs(state.yaw()) < STATIONARY_THRESHOLD
			&& Math.abs(state.roll()) < STATIONARY_THRESHOLD;
	}
}
