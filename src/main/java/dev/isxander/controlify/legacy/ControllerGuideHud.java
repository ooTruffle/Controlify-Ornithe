package dev.isxander.controlify.legacy;

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.TextHud;

public final class ControllerGuideHud extends TextHud {
	public static final String MENU_POINTER_GUIDE = "LS / D-Pad: Cursor    B: Back";
	public static final String MENU_BUTTONS_GUIDE = "A: Left Click    X: Right Click    LB/RB: Scroll";

	private static final String GAMEPLAY_GUIDE = """
			LS Move   RS Look   LT Use   RT Attack
			A Jump   B Sneak   L3 Sprint
			Y Inventory   X Drop   LB/RB Hotbar
			D-Pad ↑ Chat   ↓ View   → Players
			R3 Pick Block   Start Pause""";

	public ControllerGuideHud() {
		super("controller_guide.json", "Controller Guide", Hud.Category.getINFO(), "", "");
	}

	@Override
	protected String getText() {
		if (!LegacyConfig.showControllerGuide) return null;
		Minecraft minecraft = Minecraft.getInstance();
		boolean editing = HudManager.INSTANCE.isEditing();
		if (!ControllerManager.isConnected() && !editing) return null;
		if (minecraft.screen != null) {
			return MENU_POINTER_GUIDE + "\n" + MENU_BUTTONS_GUIDE;
		}
		return minecraft.player != null || editing ? GAMEPLAY_GUIDE : null;
	}

	@Override
	public boolean showByDefault() {
		return true;
	}

	@Override
	public kotlin.Pair<Float, Float> defaultPosition() {
		return new kotlin.Pair<>(6.0F, 6.0F);
	}
}
