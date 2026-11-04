package app.template.extension.shatteredpixeldungeon;

@SuppressWarnings("unused")
public class UnlockAll {

	/**
	 * Whether the given journal document is one of the guide books rather than a lore document.
	 */
	public static boolean isGuideDocument(Object document) {
		if (!(document instanceof Enum)) return false;
		String name = ((Enum<?>) document).name();
		return name.equals("ADVENTURERS_GUIDE") || name.equals("ALCHEMY_GUIDE");
	}
}
