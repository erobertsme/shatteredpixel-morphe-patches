package app.template.extension.shatteredpixeldungeon;

/**
 * The quality of life toggles in the Extras menu.
 */
final class QolSettings {

	private static final String KEY_FEARLESS_OFF = "qol_fearless_shopkeeper_off";
	private static final String KEY_TILE_LIST_OFF = "qol_tile_contents_off";
	private static final String KEY_UNLIMITED = "qol_unlimited_inventory";

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

	static boolean tileContents() {
		return isAvailable() && !CloverlordSettings.get(KEY_TILE_LIST_OFF);
	}

	static void tileContents(boolean on) {
		CloverlordSettings.set(KEY_TILE_LIST_OFF, !on);
	}

	static boolean unlimitedInventory() {
		return isAvailable() && CloverlordSettings.get(KEY_UNLIMITED);
	}

	static void unlimitedInventory(boolean on) {
		CloverlordSettings.set(KEY_UNLIMITED, on);
	}
}
