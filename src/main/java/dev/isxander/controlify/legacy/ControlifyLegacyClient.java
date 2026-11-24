package dev.isxander.controlify.legacy;

import net.fabricmc.api.ClientModInitializer;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;

public final class ControlifyLegacyClient implements ClientModInitializer {
	private static ControllerGuideHud controllerGuideHud;

	public static ControllerGuideHud getControllerGuideHud() {
		return controllerGuideHud;
	}

	@Override
	public void onInitializeClient() {
		ControlifyLegacy.LOGGER.info("Controlify legacy port initialized");
		LegacyConfig.INSTANCE.preload();
		controllerGuideHud = new ControllerGuideHud();
		HudManager.INSTANCE.register(controllerGuideHud);
	}
}
