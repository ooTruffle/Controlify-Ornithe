package dev.isxander.controlify.legacy;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.input.Keyboard;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Adapts upstream's full keyboard layout to the vanilla 1.8.9 chat field. */
final class ControllerChatScreen extends ChatScreen {
	private static final List<List<Key>> ROWS = readLayout();
	private static final int KEY_HEIGHT = 17;
	private boolean shift;
	private boolean shiftLock;

	@Override
	public void init() {
		super.init();
		rebuildKeyboard();
	}

	private void rebuildKeyboard() {
		buttons.clear();
		int unit = Math.max(1, Math.min(24, (width - 8) / 12));
		int xStart = (width - unit * 12) / 2;
		int y = height - 18 - KEY_HEIGHT * ROWS.size();
		int id = 0;
		for (List<Key> row : ROWS) {
			int x = xStart;
			for (Key key : row) {
				int keyWidth = Math.round(key.width * unit);
				buttons.add(new ButtonWidget(id++, x, y, keyWidth - 1, KEY_HEIGHT - 1, key.label(shift || shiftLock)));
				x += keyWidth;
			}
			y += KEY_HEIGHT;
		}
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		int id = button.id;
		for (List<Key> row : ROWS) {
			if (id < row.size()) {
				press(row.get(id));
				return;
			}
			id -= row.size();
		}
	}

	boolean shortcut(ControllerButton button) {
		String shortcut = switch (button) {
			case X -> "controlify:gui_abstract_action_1";
			case Y -> "controlify:gui_abstract_action_2";
			case LEFT_STICK -> "controlify:gui_abstract_action_3";
			case START -> "controlify:pause";
			default -> "";
		};
		for (List<Key> row : ROWS) {
			for (Key key : row) {
				if (key.shortcut != null && key.shortcut.equals(shortcut)) {
					press(key);
					return true;
				}
			}
		}
		return false;
	}

	private void press(Key key) {
		chatField.setFocused(true);
		switch (key.action) {
			case "shift" -> {
				shift = !shift;
				rebuildKeyboard();
			}
			case "shift_lock" -> {
				shiftLock = !shiftLock;
				shift = false;
				rebuildKeyboard();
			}
			case "enter" -> keyPressed('\r', Keyboard.KEY_RETURN);
			case "backspace" -> chatField.keyPressed('\b', Keyboard.KEY_BACK);
			default -> {
				chatField.write(key.characters(shift || shiftLock));
				if (shift) {
					shift = false;
					rebuildKeyboard();
				}
			}
		}
	}

	private static List<List<Key>> readLayout() {
		var stream = ControllerChatScreen.class.getResourceAsStream("/assets/controlify/keyboard_layout/full/en_us.json");
		var rows = new JsonParser().parse(new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8))
				.getAsJsonObject().getAsJsonArray("keys");
		List<List<Key>> layout = new ArrayList<>();
		for (JsonElement row : rows) {
			List<Key> keys = new ArrayList<>();
			for (JsonElement key : row.getAsJsonArray()) keys.add(Key.read(key));
			layout.add(List.copyOf(keys));
		}
		return List.copyOf(layout);
	}

	private record Key(String regular, String shifted, String action, String displayName, float width, String shortcut) {
		static Key read(JsonElement element) {
			if (element.isJsonPrimitive()) {
				String value = element.getAsString();
				return new Key(value, value.toUpperCase(Locale.ROOT), "", null, 1, null);
			}
			if (element.isJsonArray()) {
				var pair = element.getAsJsonArray();
				return new Key(pair.get(0).getAsString(), pair.get(1).getAsString(), "", null, 1, null);
			}
			var key = element.getAsJsonObject();
			var role = key.getAsJsonObject("regular");
			String chars = role.has("chars") ? role.get("chars").getAsString() : "";
			String action = role.has("action") ? role.get("action").getAsString() : "";
			String label = role.has("display_name") ? role.get("display_name").getAsString() : switch (action) {
				case "shift" -> "Shift";
				case "shift_lock" -> "Caps";
				case "enter" -> "Enter";
				case "backspace" -> "Backspace";
				default -> null;
			};
			return new Key(chars, chars, action, label, key.get("width").getAsFloat(),
					key.has("shortcut") ? key.get("shortcut").getAsString() : null);
		}

		String characters(boolean shift) {
			return shift ? shifted : regular;
		}

		String label(boolean shift) {
			return displayName == null ? characters(shift) : displayName;
		}
	}
}
