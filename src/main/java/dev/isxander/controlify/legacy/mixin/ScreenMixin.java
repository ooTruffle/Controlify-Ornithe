package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import dev.isxander.controlify.legacy.ControlifyLegacyClient;
import dev.isxander.controlify.legacy.ControllerGuideHud;
import dev.isxander.controlify.legacy.LegacyConfig;
import dev.isxander.controlify.legacy.RadialMenuScreen;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends GuiElement {
	@Shadow protected TextRenderer textRenderer;
	@Shadow protected List<ButtonWidget> buttons;

	@Invoker("keyPressed") protected abstract void controlify$pressEscape(char character, int keyCode);

	@Inject(method = "handleInputs", at = @At("HEAD"))
	private void controlify$navigateMenus(CallbackInfo ci) {
		Screen screen = (Screen) (Object) this;
		if (screen instanceof RadialMenuScreen) return;
		if (!ControllerManager.canNavigateMenu()) {
			ControllerManager.releaseMenuButton();
			return;
		}
		boolean resume = screen instanceof GameMenuScreen && ControllerManager.consumePausePress();
		if (resume || ControllerManager.backPressed()) {
			ControllerManager.releaseMenuButton();
			controlify$pressEscape('\0', 1);
		} else {
			ControllerManager.handleMenuInput(screen, buttons);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void controlify$drawMenuGuide(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ControllerManager.isControllerInput() || (Object) this instanceof RadialMenuScreen) return;
		ControllerGuideHud hud = ControlifyLegacyClient.getControllerGuideHud();
		if (!HudManager.INSTANCE.isEditing() && LegacyConfig.showControllerGuide && hud != null &&
				hud.getShowInScreens() && !hud.getHidden() && textRenderer != null) {
			int x = Math.round(hud.getX());
			int y = Math.round(hud.getY());
			String[] lines = ControllerGuideHud.menuGuide((Screen) (Object) this);
			int width = 0;
			for (String line : lines) width = Math.max(width, textRenderer.getWidth(line));
			GuiElement.fill(x, y, x + width + 8, y + 6 + 11 * lines.length, 0xA9000000);
			for (int i = 0; i < lines.length; i++) {
				drawString(textRenderer, lines[i], x + 4, y + 4 + 11 * i, 0xFFFFFF);
			}
		}
	}
}
