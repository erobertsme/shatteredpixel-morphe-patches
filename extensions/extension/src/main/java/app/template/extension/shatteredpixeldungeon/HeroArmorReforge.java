package app.template.extension.shatteredpixeldungeon;

import java.lang.reflect.Field;

/**
 * Lets the blacksmith reforge hero armor, the armor the dwarven crown turns yours into, with armor of the tier it was before.
 */
public final class HeroArmorReforge {

	private static final String KEY_OFF = "qol_hero_armor_reforge_off";
	private static final String ARMOR = "com.shatteredpixel.shatteredpixeldungeon.items.armor.";
	private static final String[] TIERS = { "ClothArmor", "LeatherArmor", "MailArmor", "ScaleArmor", "PlateArmor" };

	private HeroArmorReforge() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && !CloverlordSettings.get(KEY_OFF);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_OFF, !on);
	}

	/**
	 * Called in place of getClass when the reforge window checks that two items are the same kind. Hero armor counts as the armor of its
	 * tier.
	 */
	public static Class<?> kind(Object item) {
		Class<?> type = item.getClass();
		if (!isOn()) return type;
		try {
			if (!Class.forName(ARMOR + "ClassArmor").isInstance(item)) return type;
			Field tier = Reflect.field(type, "tier");
			int index = tier.getInt(item) - 1;
			return index >= 0 && index < TIERS.length ? Class.forName(ARMOR + TIERS[index]) : type;
		} catch (Throwable e) {
			return type;
		}
	}
}
