package dev.isxander.controlify.legacy;

import dev.isxander.controlify.legacy.mixin.InventoryMenuScreenAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.inventory.slot.InventorySlot;
import org.lwjgl.input.Mouse;

final class InventoryNavigation {
	private InventoryNavigation() {
	}

	static void snap(InventoryMenuScreen screen, ControllerMenu cursor, int dx, int dy) {
		if (dx == 0 && dy == 0) return;
		// When two directions arrive on the same tick, move along the vertical axis.
		if (dy != 0) dx = 0;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.width <= 0 || minecraft.height <= 0 || screen.width <= 0 || screen.height <= 0) return;
		var access = (InventoryMenuScreenAccess) screen;
		int mouseX = mouseX(screen, minecraft);
		int mouseY = mouseY(screen, minecraft);
		int left = access.controlify$getLeft();
		int top = access.controlify$getTop();
		InventorySlot current = access.controlify$getSlot(mouseX, mouseY);
		int originX = current == null ? mouseX - left - 8 : current.x;
		int originY = current == null ? mouseY - top - 8 : current.y;
		InventorySlot next = null;
		int bestScore = Integer.MAX_VALUE;
		for (InventorySlot slot : screen.menu.slots) {
			if (!slot.isActive()) continue;
			int offsetX = slot.x - originX;
			int offsetY = slot.y - originY;
			int score = offsetX * offsetX + offsetY * offsetY;
			if (current != null) {
				int forward = offsetX * dx + offsetY * dy;
				if (forward <= 0) continue;
				int lateral = offsetX * dy - offsetY * dx;
				score += 3 * lateral * lateral;
			}
			if (score < bestScore) {
				next = slot;
				bestScore = score;
			}
		}
		if (next != null) {
			positionCursor(screen, cursor, next, left, top);
		}
	}

	static void snapToClosest(InventoryMenuScreen screen, ControllerMenu cursor) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.width <= 0 || minecraft.height <= 0 || screen.width <= 0 || screen.height <= 0) return;
		var access = (InventoryMenuScreenAccess) screen;
		InventorySlot slot = access.controlify$getSlot(mouseX(screen, minecraft), mouseY(screen, minecraft));
		if (slot != null && slot.isActive()) {
			positionCursor(screen, cursor, slot, access.controlify$getLeft(), access.controlify$getTop());
		}
	}

	private static void positionCursor(InventoryMenuScreen screen, ControllerMenu cursor, InventorySlot slot, int left, int top) {
		Minecraft minecraft = Minecraft.getInstance();
		cursor.positionCursor((left + slot.x + 8) * minecraft.width / screen.width,
				minecraft.height - 1 - (top + slot.y + 8) * minecraft.height / screen.height);
	}

	static boolean quickMove(InventoryMenuScreen screen) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.width <= 0 || minecraft.height <= 0
				|| minecraft.player.inventory.getCursorItem() != null) return false;
		var access = (InventoryMenuScreenAccess) screen;
		InventorySlot slot = access.controlify$getSlot(mouseX(screen, minecraft), mouseY(screen, minecraft));
		if (slot == null || !slot.isActive() || !slot.hasItem() || !slot.canPickUp(minecraft.player)) return false;
		// Mouse button 0 with click mode 1 is vanilla's shift-click operation.
		access.controlify$clickSlot(slot, slot.index, 0, 1);
		return true;
	}

	private static int mouseX(InventoryMenuScreen screen, Minecraft minecraft) {
		return Mouse.getX() * screen.width / minecraft.width;
	}

	private static int mouseY(InventoryMenuScreen screen, Minecraft minecraft) {
		return screen.height - 1 - Mouse.getY() * screen.height / minecraft.height;
	}
}
