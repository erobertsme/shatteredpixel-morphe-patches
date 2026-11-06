package app.template.extension.shatteredpixeldungeon;

/**
 * The quality of life toggles in the Extras menu.
 */
final class QolSettings {


	private QolSettings() {
	}

	static final int DEFAULT_BAG_ROWS = 4;
	static final int MAX_BAG_ROWS = 8;
	private static final int BAG_COLUMNS = 5;
	private static final int DEFAULT_BAG_SLOTS = DEFAULT_BAG_ROWS * BAG_COLUMNS;
	private static final String KEY_BAG_ROWS = "qol_bag_rows";
	private static final String KEY_EXTRA_PAGE = "qol_bag_extra_page";

	/**
	 * Rows of items on each page of the backpack and every bag. The game shows 4.
	 */
	static int bagRows() {
		if (!isAvailable()) return DEFAULT_BAG_ROWS;
		int rows = CloverlordSettings.getInt(KEY_BAG_ROWS);
		return rows <= 0 ? DEFAULT_BAG_ROWS : Math.max(DEFAULT_BAG_ROWS, Math.min(MAX_BAG_ROWS, rows));
	}

	static void bagRows(int rows) {
		CloverlordSettings.setInt(KEY_BAG_ROWS, Math.max(DEFAULT_BAG_ROWS, Math.min(MAX_BAG_ROWS, rows)));
	}

	static boolean extraBagPage() {
		return isAvailable() && CloverlordSettings.get(KEY_EXTRA_PAGE);
	}

	static void extraBagPage(boolean on) {
		CloverlordSettings.set(KEY_EXTRA_PAGE, on);
	}

	/**
	 * Item slots in each bag across its pages.
	 */
	static int bagSlots() {
		return bagRows() * BAG_COLUMNS * (extraBagPage() ? 2 : 1);
	}

	/**
	 * How many more items each bag holds than in the normal game.
	 */
	static int extraBagSlots() {
		return Math.max(0, bagSlots() - DEFAULT_BAG_SLOTS);
	}

	static boolean isAvailable() {
		return PathPreview.isAvailable();
	}


}
