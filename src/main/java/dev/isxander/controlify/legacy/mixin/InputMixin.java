package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.entity.living.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class InputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void controlify$applyAnalogMovement(CallbackInfo ci) {
		ControllerManager.combineAnalogMovement((KeyboardInput) (Object) this);
	}
}
