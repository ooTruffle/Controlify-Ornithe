package dev.isxander.controlify.legacy;

import org.polyfrost.oneconfig.api.config.v1.Config;
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
			title = "Show controller guide",
			category = "Controller",
			description = "Show controller controls on screen while playing and in menus."
	)
	public static boolean showControllerGuide = true;

	static float cameraSpeedMultiplier() {
		return Math.clamp(cameraSpeedPercent, MIN_CAMERA_SPEED, MAX_CAMERA_SPEED) / 100.0F;
	}

	private LegacyConfig() {
		super("controlify-legacy.json", "Controlify", Category.UTILITY);
	}
}
