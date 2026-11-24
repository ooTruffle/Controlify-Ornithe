package dev.isxander.controlify.legacy.mixin;

import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.inventory.slot.InventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InventoryMenuScreen.class)
public interface InventoryMenuScreenAccess {
	@Accessor("x") int controlify$getLeft();
	@Accessor("y") int controlify$getTop();
	@Invoker("getSlot") InventorySlot controlify$getSlot(int x, int y);
	@Invoker("clickSlot") void controlify$clickSlot(InventorySlot slot, int index, int button, int mode);
}
