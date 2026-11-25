package dev.isxander.controlify.legacy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.isxander.controlify.controller.input.GamepadInputs;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

final class DefaultBindings {
	private static final JsonObject DEFAULTS;

	static {
		var stream = DefaultBindings.class.getResourceAsStream("/assets/controlify/controllers/default_bind/default.json");
		DEFAULTS = new JsonParser().parse(new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8))
				.getAsJsonObject().getAsJsonObject("defaults");
	}

	private DefaultBindings() {
	}

	static ControllerButton get(String action) {
		JsonObject input = DEFAULTS.getAsJsonObject("controlify:" + action);
		if (input == null) return ControllerButton.UNBOUND;
		if (input.has("axis")) {
			String axis = input.get("axis").getAsString();
			if (axis.equals(GamepadInputs.LEFT_TRIGGER_AXIS.toString())) return ControllerButton.LEFT_TRIGGER;
			if (axis.equals(GamepadInputs.RIGHT_TRIGGER_AXIS.toString())) return ControllerButton.RIGHT_TRIGGER;
		}
		if (!input.has("button")) return ControllerButton.UNBOUND;
		String id = input.get("button").getAsString();
		return Arrays.stream(ControllerButton.DIGITAL_BUTTONS)
				.filter(button -> button.input.toString().equals(id))
				.findFirst().orElse(ControllerButton.UNBOUND);
	}
}
