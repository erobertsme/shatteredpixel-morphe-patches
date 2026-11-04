package app.template.extension.shatteredpixeldungeon;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Favorite items and statuses for the add pickers. Starts with commonly used ones until the player changes them.
 */
final class Favorites {

	private static final String PACKAGE = "com.shatteredpixel.shatteredpixeldungeon.";
	private static final String KEY_ITEMS = "favorite_items";
	private static final String KEY_STATUSES = "favorite_statuses";

	private static final String[] DEFAULT_ITEMS = {
		"items.potions.PotionOfHealing",
		"items.potions.PotionOfStrength",
		"items.potions.PotionOfExperience",
		"items.scrolls.ScrollOfUpgrade",
		"items.scrolls.ScrollOfIdentify",
		"items.scrolls.ScrollOfRemoveCurse",
		"items.scrolls.ScrollOfMagicMapping",
		"items.scrolls.ScrollOfTeleportation",
		"items.food.Food",
		"items.stones.StoneOfEnchantment",
		"items.stones.StoneOfAugmentation",
		"items.Ankh",
		"items.Honeypot",
		"items.Torch",
	};

	private static final String[] DEFAULT_STATUSES = {
		"actors.buffs.Haste",
		"actors.buffs.Invisibility",
		"actors.buffs.Levitation",
		"actors.buffs.MindVision",
		"actors.buffs.Bless",
		"actors.buffs.Barkskin",
		"actors.buffs.Barrier",
		"actors.buffs.Recharging",
		"actors.buffs.Healing",
		"actors.buffs.Adrenaline",
		"actors.buffs.ArcaneArmor",
		"actors.buffs.Light",
	};

	private static Set<String> items;
	private static Set<String> statuses;

	private Favorites() {
	}

	static boolean isItem(Class<?> type) {
		return items().contains(type.getName());
	}

	static void toggleItem(Class<?> type) {
		toggle(items(), type.getName(), KEY_ITEMS);
	}

	static boolean isStatus(Class<?> type) {
		return statuses().contains(type.getName());
	}

	static void toggleStatus(Class<?> type) {
		toggle(statuses(), type.getName(), KEY_STATUSES);
	}

	private static synchronized Set<String> items() {
		if (items == null) items = load(KEY_ITEMS, DEFAULT_ITEMS);
		return items;
	}

	private static synchronized Set<String> statuses() {
		if (statuses == null) statuses = load(KEY_STATUSES, DEFAULT_STATUSES);
		return statuses;
	}

	private static Set<String> load(String key, String[] defaults) {
		String stored = CloverlordSettings.getString(key);
		Set<String> result = new LinkedHashSet<>();
		if (stored == null) {
			for (String name : defaults) result.add(PACKAGE + name);
			return result;
		}
		if (!stored.isEmpty()) result.addAll(Arrays.asList(stored.split(";")));
		return result;
	}

	private static void toggle(Set<String> set, String name, String key) {
		if (!set.remove(name)) set.add(name);
		CloverlordSettings.setString(key, String.join(";", set));
	}
}
