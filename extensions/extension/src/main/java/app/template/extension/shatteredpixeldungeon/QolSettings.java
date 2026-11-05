package app.template.extension.shatteredpixeldungeon;

/**
 * The quality of life toggles in the Extras menu.
 */
final class QolSettings {


	private QolSettings() {
	}

	static boolean isAvailable() {
		return PathPreview.isAvailable();
	}


}
