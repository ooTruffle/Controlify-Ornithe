package dev.isxander.controlify.legacy;

/** Converts stick and gyroscope input into vanilla camera deltas at the render rate. */
final class ControllerLook {
	private static final double RESPONSE = 20.0;
	private static final double MAX_FRAME_SECONDS = 0.05;
	private static final double VANILLA_DEGREES_PER_UNIT = 0.15;
	private static final float GYRO_NOISE_THRESHOLD = 0.02F;

	private long lastFrameNanos;
	private double frameSeconds;
	private double smoothedX;
	private double smoothedY;
	float turnX;
	float turnY;

	void update(float x, float y, long now, float mouseSensitivity, float speedMultiplier, float deadzone, boolean invertY) {
		double elapsed = lastFrameNanos == 0L ? 0.0 : (now - lastFrameNanos) / 1_000_000_000.0;
		lastFrameNanos = now;
		elapsed = Math.min(elapsed, MAX_FRAME_SECONDS);
		frameSeconds = elapsed;

		double magnitude = Math.hypot(x, y);
		double strength = Math.max(0.0, (Math.min(magnitude, 1.0) - deadzone) / (1.0 - deadzone));
		double scale = strength == 0.0 ? 0.0 : Math.pow(strength, 1.15) / magnitude;
		double targetX = x * scale;
		double targetY = y * scale;

		// Integrate the exponential response over the frame to keep turn speed consistent across frame rates.
		double blend = -Math.expm1(-elapsed * RESPONSE);
		double previousWeight = blend / RESPONSE;
		double distanceX = targetX * elapsed + (smoothedX - targetX) * previousWeight;
		double distanceY = targetY * elapsed + (smoothedY - targetY) * previousWeight;
		smoothedX += (targetX - smoothedX) * blend;
		smoothedY += (targetY - smoothedY) * blend;

		double sensitivity = mouseSensitivity * 0.6 + 0.2;
		double degreesPerSecond = sensitivity * sensitivity * sensitivity * 360.0 * speedMultiplier;
		double rotationScale = degreesPerSecond / VANILLA_DEGREES_PER_UNIT;
		turnX = (float) (distanceX * rotationScale);
		// SDL's Y axis is positive down, while vanilla subtracts the vertical delta.
		turnY = (float) (distanceY * rotationScale * (invertY ? 1.0 : -1.0));
	}

	void addGyro(float pitchRate, float yawRate, float sensitivity, boolean invertY) {
		// SDL reports radians per second around the controller's local axes. Vanilla subtracts the vertical delta.
		double rotationScale = frameSeconds * sensitivity / VANILLA_DEGREES_PER_UNIT;
		turnX -= (float) (Math.toDegrees(filteredGyroRate(yawRate)) * rotationScale);
		turnY += (float) (Math.toDegrees(filteredGyroRate(pitchRate)) * rotationScale * (invertY ? -1.0 : 1.0));
	}

	private static float filteredGyroRate(float rate) {
		return Float.isFinite(rate) && Math.abs(rate) >= GYRO_NOISE_THRESHOLD ? rate : 0.0F;
	}

	void reset() {
		lastFrameNanos = 0L;
		frameSeconds = 0.0;
		smoothedX = smoothedY = 0.0;
		turnX = turnY = 0.0F;
	}
}
