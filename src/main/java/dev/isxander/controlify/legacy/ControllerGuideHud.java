package dev.isxander.controlify.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.TextHud;

public final class ControllerGuideHud extends TextHud {
	public ControllerGuideHud() {
		super("controller_guide.json", "Controller Guide", Hud.Category.getINFO(), "", "");
	}

	@Override
	protected String getText() {
		if (!LegacyConfig.showControllerGuide) return null;
		Minecraft minecraft = Minecraft.getInstance();
		boolean editing = HudManager.INSTANCE.isEditing();
		if (!ControllerManager.isControllerInput() && !editing) return null;
		if (minecraft.screen instanceof RadialMenuScreen && !editing) return null;
		if (minecraft.screen != null) {
			return String.join("\n", menuGuide(minecraft.screen));
		}
		return minecraft.player != null || editing ? gameplayGuide() : null;
	}

	public static String[] menuGuide(Screen screen) {
		String back = hint(ControllerAction.MENU_BACK, "Back");
		if (screen instanceof ControllerChatScreen) {
			return new String[]{"LS / D-Pad Choose Key   " + hint(ControllerAction.MENU_SELECT, "Type") + "   " + back,
					"X Backspace   Y Space   L3 Shift   Start Send"};
		}
		String clicks = hint(ControllerAction.MENU_SELECT, "Left Click") + "   "
				+ hint(ControllerAction.MENU_RIGHT_CLICK, "Right Click");
		if (screen instanceof InventoryMenuScreen) {
			return new String[]{"LS Cursor   D-Pad Slots   " + back,
					clicks + "   " + hint(ControllerAction.QUICK_MOVE, "Quick Move"),
					"RS Scroll"};
		}
		return new String[]{"LS Cursor   D-Pad Buttons   " + back, clicks + "   RS Scroll"};
	}

	private static String gameplayGuide() {
		return String.join("\n",
				"LS Move   RS Look   " + hint(ControllerAction.USE, "Use") + "   " + hint(ControllerAction.ATTACK, "Attack"),
				hint(ControllerAction.JUMP, "Jump") + "   " + hint(ControllerAction.SNEAK, "Sneak") + "   " + hint(ControllerAction.SPRINT, "Sprint"),
				hint(ControllerAction.INVENTORY, "Inventory") + "   " + hint(ControllerAction.DROP, "Drop") + "   "
						+ ControllerAction.HOTBAR_PREV.label() + "/" + ControllerAction.HOTBAR_NEXT.label() + " Hotbar",
				hint(ControllerAction.CHAT, "Chat") + "   " + hint(ControllerAction.PERSPECTIVE, "View") + "   " + hint(ControllerAction.RADIAL_MENU, "Radial Menu"),
				hint(ControllerAction.PICK_BLOCK, "Pick Block") + "   " + hint(ControllerAction.PAUSE, "Pause"));
	}

	private static String hint(ControllerAction action, String label) {
		return action.label() + " " + label;
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
