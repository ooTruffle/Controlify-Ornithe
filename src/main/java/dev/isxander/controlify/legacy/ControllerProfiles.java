package dev.isxander.controlify.legacy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.polyfrost.oneconfig.api.config.v1.Property;

import java.util.function.BiConsumer;

/** Saves each device's input settings in the existing OneConfig file. */
final class ControllerProfiles {
	private static final Gson GSON = new Gson();
	private static JsonObject profiles;
	private static String activeKey;
	private static boolean unavailable;

	private ControllerProfiles() {
	}

	static void activate(String key) {
		if (unavailable || key.equals(activeKey)) return;
		if (profiles == null) {
			try {
				profiles = GSON.fromJson(LegacyConfig.controllerProfilesJson, JsonObject.class);
				if (profiles == null) profiles = new JsonObject();
			} catch (JsonParseException e) {
				unavailable = true;
				ControlifyLegacy.LOGGER.warn("Could not read controller profiles", e);
				return;
			}
		}
		save();
		// Preserve settings from older builds on the first identified controller.
		if (profiles.entrySet().size() > 0) {
			LegacyConfig.applyUpstreamProfileDefaults();
			if (profiles.has(key) && profiles.get(key).isJsonObject()) {
				JsonObject profile = profiles.getAsJsonObject(key);
				forEachSetting((id, property) -> {
					if (!profile.has(id)) return;
					try {
						Object value = GSON.fromJson(profile.get(id), property.type);
						if (value != null) property.setAs(value);
					} catch (JsonParseException | IllegalArgumentException e) {
						ControlifyLegacy.LOGGER.warn("Ignoring invalid controller profile setting {}", id, e);
					}
				});
			}
		}
		activeKey = key;
	}

	static void save() {
		if (activeKey == null) return;
		JsonObject profile = new JsonObject();
		forEachSetting((id, property) -> profile.add(id, GSON.toJsonTree(property.get())));
		profiles.add(activeKey, profile);
		LegacyConfig.controllerProfilesJson = GSON.toJson(profiles);
		LegacyConfig.INSTANCE.save();
	}

	private static void forEachSetting(BiConsumer<String, Property<?>> action) {
		LegacyConfig.INSTANCE.getTree().onAllProps((id, property) -> {
			Class<?> type = property.type;
			if (type == boolean.class || type == Boolean.class || type == int.class || type == Integer.class
					|| (type != null && type.isEnum())) {
				action.accept(id, property);
			}
		});
	}
}
