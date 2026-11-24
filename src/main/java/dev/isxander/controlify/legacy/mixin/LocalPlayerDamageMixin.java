package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public abstract class LocalPlayerDamageMixin {
	@Inject(method = "damageTo", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
	private void controlify$rumbleOnDamage(float newHealth, CallbackInfo ci) {
		var player = (LocalClientPlayerEntity) (Object) this;
		ControllerManager.rumbleOnDamage(player.getHealth() - newHealth);
	}
}
