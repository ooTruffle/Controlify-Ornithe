package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import dev.isxander.controlify.legacy.ControlifyLegacyClient;
import dev.isxander.controlify.legacy.ControllerGuideHud;
import dev.isxander.controlify.legacy.LegacyConfig;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.TextRenderer;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin extends GuiElement {
	@Shadow protected TextRenderer textRenderer;

	@Invoker("keyPressed") protected abstract void controlify$pressEscape(char character, int keyCode);

	@Inject(method = "handleInputs", at = @At("HEAD"))
	private void controlify$navigateMenus(CallbackInfo ci) {
		if (!ControllerManager.canNavigateMenu()) {
			ControllerManager.releaseMenuButton();
			return;
		}
		Screen screen = (Screen) (Object) this;
		boolean resume = screen instanceof GameMenuScreen && ControllerManager.consumePausePress();
		if (resume || ControllerManager.backPressed()) {
			ControllerManager.releaseMenuButton();
			controlify$pressEscape('\0', 1);
		} else {
			ControllerManager.handleMenuInput(screen);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void controlify$drawMenuGuide(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ControllerManager.isConnected()) return;
		ControllerGuideHud hud = ControlifyLegacyClient.getControllerGuideHud();
		if (!HudManager.INSTANCE.isEditing() && LegacyConfig.showControllerGuide && hud != null &&
				hud.getShowInScreens() && !hud.getHidden() && textRenderer != null) {
			int x = Math.round(hud.getX());
			int y = Math.round(hud.getY());
			String pointerGuide = ControllerGuideHud.MENU_POINTER_GUIDE;
			String buttonsGuide = ControllerGuideHud.MENU_BUTTONS_GUIDE;
			int width = Math.max(textRenderer.getWidth(pointerGuide), textRenderer.getWidth(buttonsGuide));
			GuiElement.fill(x, y, x + width + 8, y + 28, 0xA9000000);
			drawString(textRenderer, pointerGuide, x + 4, y + 4, 0xFFFFFF);
			drawString(textRenderer, buttonsGuide, x + 4, y + 15, 0xFFFFFF);
		}
	}
}
