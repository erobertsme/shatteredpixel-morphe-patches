package app.template.extension.shatteredpixeldungeon;

/**
 * Equipping and taking off equipment without using a turn, the way the Duelist swaps weapons instantly: for every kind of equipment
 * when switched on in Extras, and always while the editors take off an equipped item to delete or replace it.
 */
public final class InstantEquip {

	private static final String KEY = "qol_instant_equip";
	private static boolean editing;

	private InstantEquip() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && CloverlordSettings.get(KEY);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY, on);
	}

	/**
	 * Called at the end of EquipableItem.timeToEquip, which weapons, armor, rings and artifacts all use. Returns no time while equipping
	 * is instant.
	 */
	public static float adjustEquipTime(float time) {
		return editing || isOn() ? 0 : time;
	}

	/**
	 * Runs [change], an editor's change to equipped items, without it using a turn.
	 */
	static void whileEditing(Runnable change) {
		editing = true;
		try {
			change.run();
		} finally {
			editing = false;
		}
	}
}
