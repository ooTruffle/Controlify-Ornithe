package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "shutdown", at = @At("HEAD"))
	private void controlify$saveControllerProfile(CallbackInfo ci) {
		ControllerManager.shutdown();
	}

	@Inject(method = "handleGuiKeyBindings", at = @At("HEAD"))
	private void controlify$keyboardUsed(CallbackInfo ci) {
		if (Keyboard.getEventKeyState()) ControllerManager.keyboardMouseUsed();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void controlify$pollController(CallbackInfo ci) {
		ControllerManager.tick((Minecraft) (Object) this);
	}

	@Inject(method = "openScreen", at = @At("HEAD"))
	private void controlify$suspendGameplayButtons(Screen nextScreen, CallbackInfo ci) {
		if (nextScreen != null || ((Minecraft) (Object) this).screen != null) {
			ControllerManager.suspendGameplayButtons();
		}
	}

	@Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJ)V"))
	private void controlify$updateControllerFrame(CallbackInfo ci) {
		ControllerManager.updateFrame((Minecraft) (Object) this);
	}

	@Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJ)V", shift = At.Shift.AFTER))
	private void controlify$captureScreenshot(CallbackInfo ci) {
		ControllerManager.afterRender((Minecraft) (Object) this);
	}
}
