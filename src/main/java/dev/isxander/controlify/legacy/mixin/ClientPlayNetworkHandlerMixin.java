package dev.isxander.controlify.legacy.mixin;

import dev.isxander.controlify.legacy.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "handleExplosion", at = @At("RETURN"))
	private void controlify$rumbleOnExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		double dx = player.x - packet.getX();
		double dy = player.y - packet.getY();
		double dz = player.z - packet.getZ();
		ControllerManager.rumbleOnExplosion(Math.sqrt(dx * dx + dy * dy + dz * dz), packet.getPower());
	}
}
