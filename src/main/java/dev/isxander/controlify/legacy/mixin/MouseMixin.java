package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
	@Shadow public int x;
	@Shadow public int y;

	@Inject(method = "tick", at = @At("TAIL"))
	private void controlify$mouseUsed(CallbackInfo ci) {
		if (x != 0 || y != 0) ControllerManager.keyboardMouseUsed();
	}
}
