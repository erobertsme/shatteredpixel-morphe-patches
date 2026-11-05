package app.template.extension.shatteredpixeldungeon;

/**
 * The quality of life toggles in the Extras menu.
 */
final class QolSettings {

	private static final String KEY_FEARLESS_OFF = "qol_fearless_shopkeeper_off";

	private QolSettings() {
	}

	static boolean isAvailable() {
		return PathPreview.isAvailable();
	}

	static boolean fearlessShopkeeper() {
		return isAvailable() && !CloverlordSettings.get(KEY_FEARLESS_OFF);
	}

	static void fearlessShopkeeper(boolean on) {
		CloverlordSettings.set(KEY_FEARLESS_OFF, !on);
	}

}
