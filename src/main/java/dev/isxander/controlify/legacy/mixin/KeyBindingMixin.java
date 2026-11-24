package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	@Inject(method = "isPressed", at = @At("RETURN"), cancellable = true)
	private void controlify$mergeHeldInput(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && ControllerManager.isBindingHeld((KeyBinding) (Object) this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "consumeClick", at = @At("RETURN"), cancellable = true)
	private void controlify$mergeClick(CallbackInfoReturnable<Boolean> cir) {
		if (ControllerManager.consumeBindingClick((KeyBinding) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
