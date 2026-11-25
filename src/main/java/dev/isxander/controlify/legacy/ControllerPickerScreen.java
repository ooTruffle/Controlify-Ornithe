package dev.isxander.controlify.legacy;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.input.Keyboard;

import java.util.List;

final class ControllerPickerScreen extends Screen {
	private static final int AUTOMATIC = -1;
	private static final int BACK = -2;
	private static final int PREVIOUS = -3;
	private static final int NEXT = -4;
	private static final int LIST_TOP = 68;
	private static final int ROW_HEIGHT = 23;

	private final Screen parent;
	private List<ControllerManager.GamepadDevice> devices = List.of();
	private int page;
	private int refreshTicks;
	private int displayedActiveId;
	private boolean displayedAutomatic;

	ControllerPickerScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void init() {
		devices = ControllerManager.connectedGamepads();
		rebuildButtons();
	}

	@Override
	public void tick() {
		if (++refreshTicks < 10) return;
		refreshTicks = 0;
		var connected = ControllerManager.connectedGamepads();
		if (!devices.equals(connected) || displayedActiveId != ControllerManager.activeGamepadId()
				|| displayedAutomatic != ControllerManager.isAutomaticSelection()) {
			devices = connected;
			rebuildButtons();
		}
	}

	private void rebuildButtons() {
		displayedActiveId = ControllerManager.activeGamepadId();
		displayedAutomatic = ControllerManager.isAutomaticSelection();
		int pageSize = Math.max(1, (height - LIST_TOP - 27) / ROW_HEIGHT);
		int lastPage = Math.max(0, (devices.size() - 1) / pageSize);
		page = Math.clamp(page, 0, lastPage);
		int rowWidth = Math.min(width - 20, 240);
		int left = (width - rowWidth) / 2;
		buttons.clear();
		buttons.add(new ButtonWidget(AUTOMATIC, left, 43, rowWidth, 20,
				displayedAutomatic ? "Automatic (selected)" : "Automatic"));
		int first = page * pageSize;
		int last = Math.min(first + pageSize, devices.size());
		for (int index = first; index < last; index++) {
			var device = devices.get(index);
			String marker = device.id() == displayedActiveId ? "* " : "";
			String name = marker + (index + 1) + ". " + device.name();
			buttons.add(new ButtonWidget(index, left, LIST_TOP + (index - first) * ROW_HEIGHT, rowWidth, 20,
					textRenderer.trim(name, rowWidth - 12)));
		}
		int footer = height - 23;
		int column = rowWidth / 3;
		var previous = new ButtonWidget(PREVIOUS, left, footer, column - 3, 20, "Previous");
		var next = new ButtonWidget(NEXT, left + 2 * column + 3, footer, column - 3, 20, "Next");
		previous.active = page > 0;
		next.active = page < lastPage;
		buttons.add(previous);
		buttons.add(new ButtonWidget(BACK, left + column, footer, column, 20, "Back"));
		buttons.add(next);
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case BACK -> minecraft.openScreen(parent);
			case AUTOMATIC -> {
				ControllerManager.selectController(null, devices);
				rebuildButtons();
			}
			case PREVIOUS, NEXT -> {
				page += button.id == PREVIOUS ? -1 : 1;
				rebuildButtons();
			}
			default -> {
				ControllerManager.selectController(devices.get(button.id), devices);
				rebuildButtons();
			}
		}
	}

	@Override
	protected void keyPressed(char character, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) minecraft.openScreen(parent);
		else super.keyPressed(character, keyCode);
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		renderBackground();
		drawCenteredString(textRenderer, "Choose Controller", width / 2, 14, 0xFFFFFF);
		String active = ControllerManager.getGamepadName();
		String status = active != null ? "Active: " + active
				: displayedAutomatic ? "No active controller" : "Waiting for the selected controller";
		drawCenteredString(textRenderer, textRenderer.trim(status, width - 20), width / 2, 29, 0xCCCCCC);
		if (devices.isEmpty()) drawCenteredString(textRenderer, "No controllers detected", width / 2, LIST_TOP + 5, 0xCCCCCC);
		super.render(mouseX, mouseY, tickDelta);
	}
}
