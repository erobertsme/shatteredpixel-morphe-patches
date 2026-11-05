package app.template.extension.shatteredpixeldungeon;

/**
 * Which tap actions ask for confirmation before they happen.
 */
final class ConfirmSettings {

	enum Action {
		WALK("Walking to a distant tile", "confirm_walk", true),
		STEP("Single steps", "confirm_step", false),
		ATTACK("Attacking", "confirm_attack", true),
		INTERACT("Talking to allies and NPCs", "confirm_interact", false),
		STAIRS("Using stairs and exits", "confirm_stairs", true),
		DANGER("Stepping into danger", "confirm_danger", true);

		final String label;
		final String key;
		final boolean byDefault;

		Action(String label, String key, boolean byDefault) {
			this.label = label;
			this.key = key;
			this.byDefault = byDefault;
		}
	}

	private static final String KEY_OFF = "qol_confirm_movement_off";

	private ConfirmSettings() {
	}

	static boolean isOn() {
		return !CloverlordSettings.get(KEY_OFF);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_OFF, !on);
	}

	static boolean confirms(Action action) {
		boolean changed = CloverlordSettings.get(action.key + "_changed");
		return changed ? CloverlordSettings.get(action.key) : action.byDefault;
	}

	static void setConfirms(Action action, boolean value) {
		CloverlordSettings.set(action.key, value);
		CloverlordSettings.set(action.key + "_changed", true);
	}
}
