package app.template.extension.shatteredpixeldungeon;

@SuppressWarnings("unused")
public final class FearlessShopkeeper {

	private FearlessShopkeeper() {
	}

	/**
	 * Called at the start of Shopkeeper.flee. Returns true to keep the shopkeeper and the shop.
	 */
	public static boolean stays() {
		return QolSettings.fearlessShopkeeper();
	}
}
