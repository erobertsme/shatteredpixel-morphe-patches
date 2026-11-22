package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Bundle;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * The tier of a weapon or armor, which sets its damage or defense and the strength it needs. A changed tier is saved with the item, since
 * the game sets the tier from the item's kind when it is made.
 */
final class ItemTier {

	private static final String TAG = "ItemTier";
	private static final String KEY = "morphe_tier";
	static final int MIN = 1;
	static final int MAX = 5;
	private static final Map<Class<?>, Integer> defaults = new HashMap<>();

	private ItemTier() {
	}

	static boolean supports(Item item) {
		return item instanceof MeleeWeapon || item instanceof MissileWeapon || item instanceof Armor;
	}

	static int of(Item item) {
		try {
			return field(item).getInt(item);
		} catch (Throwable e) {
			return 0;
		}
	}

	static void set(Item item, int tier) {
		try {
			field(item).setInt(item, Math.max(MIN, Math.min(MAX, tier)));
		} catch (Throwable e) {
			Log.e(TAG, "Could not set the tier of " + item.getClass().getName(), e);
		}
		Item.updateQuickslot();
	}

	/**
	 * Puts [item] back to the tier its kind has.
	 */
	static void reset(Item item) {
		int tier = defaultOf(item.getClass());
		if (tier > 0) set(item, tier);
	}

	private static Field field(Item item) throws NoSuchFieldException {
		return Reflect.field(item.getClass(), "tier");
	}

	private static synchronized int defaultOf(Class<?> type) {
		Integer known = defaults.get(type);
		if (known != null) return known;
		int tier;
		try {
			tier = of((Item) type.getDeclaredConstructor().newInstance());
		} catch (Throwable e) {
			tier = 0;
		}
		defaults.put(type, tier);
		return tier;
	}

	static void onStore(Object item, Bundle bundle) {
		if (!(item instanceof Item) || !supports((Item) item)) return;
		int tier = of((Item) item);
		if (tier != defaultOf(item.getClass())) bundle.put(KEY, tier);
	}

	static void onRestore(Object item, Bundle bundle) {
		if (!(item instanceof Item) || !supports((Item) item) || !bundle.contains(KEY)) return;
		set((Item) item, bundle.getInt(KEY));
	}
}
