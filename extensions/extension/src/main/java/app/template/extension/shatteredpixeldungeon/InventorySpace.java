package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;

import java.lang.reflect.Field;

/**
 * Unlimited inventory space: every bag always has a free slot, and the backpack window grows to fit.
 */
@SuppressWarnings("unused")
public final class InventorySpace {

	private static final String TAG = "InventorySpace";
	private static final int EQUIPMENT_SLOTS = 5;
	private static final int SLOT_MARGIN = 1;
	private static final int TITLE_HEIGHT = 14;
	private static final int SCREEN_MARGIN = 32;
	private static final int MIN_SLOT_SIZE = 16;
	private static final int MAX_LANDSCAPE_COLUMNS = 10;

	private InventorySpace() {
	}

	/**
	 * Wraps the returns of Bag.capacity and the capacity of each special bag.
	 */
	public static int capacity(int original, Bag bag) {
		if (bag instanceof Belongings.Backpack) return original;
		return expand(original, bag);
	}

	/**
	 * Wraps the returns of Belongings.Backpack.capacity.
	 */
	public static int backpackCapacity(int original, Bag bag) {
		return expand(original, bag);
	}

	private static int expand(int original, Bag bag) {
		if (!QolSettings.unlimitedInventory() || bag == null || bag.items == null) return original;
		return Math.max(original, bag.items.size() + 1);
	}

	/**
	 * Called in the WndBag constructor once the rows are known. Returns the number of rows to lay out, and widens
	 * or shrinks the slots so they fit on screen.
	 */
	public static int rows(Object window, Bag bag, int columns, int rows) {
		try {
			if (!QolSettings.unlimitedInventory() || bag == null || columns <= 0) return rows;
			int slots = slotCount(bag);
			int needed = divideUp(slots, columns);
			if (needed <= rows) return rows;

			if (PixelScene.landscape()) {
				int slotWidth = getInt(window, "slotWidth");
				int maxColumns = Math.min(MAX_LANDSCAPE_COLUMNS, (PixelScene.uiCamera.width - SCREEN_MARGIN) / (slotWidth + SLOT_MARGIN));
				while (columns < maxColumns && divideUp(slots, columns) > rows) columns++;
				setInt(window, "nCols", columns);
				needed = Math.max(rows, divideUp(slots, columns));
			}

			int available = PixelScene.uiCamera.height - SCREEN_MARGIN - TITLE_HEIGHT - (needed - 1) * SLOT_MARGIN;
			int fit = Math.max(MIN_SLOT_SIZE, available / needed);
			if (fit < getInt(window, "slotHeight")) setInt(window, "slotHeight", fit);
			return Math.max(rows, needed);
		} catch (Throwable e) {
			Log.e(TAG, "Could not lay out the bag", e);
			return rows;
		}
	}

	private static int slotCount(Bag bag) {
		Hero hero = Dungeon.hero;
		boolean backpack = hero != null && bag == hero.belongings.backpack;
		int extra = backpack ? (hero.belongings.secondWep != null ? 1 : 0) : 1;
		int items = 0;
		for (Item item : bag.items) if (!(item instanceof Bag)) items++;
		int free = Math.max(0, bag.capacity() - bag.items.size());
		return EQUIPMENT_SLOTS + extra + items + free;
	}

	private static int divideUp(int value, int divisor) {
		return (value + divisor - 1) / divisor;
	}

	private static int getInt(Object target, String name) throws ReflectiveOperationException {
		return field(target, name).getInt(target);
	}

	private static void setInt(Object target, String name, int value) throws ReflectiveOperationException {
		field(target, name).setInt(target, value);
	}

	private static Field field(Object target, String name) throws NoSuchFieldException {
		for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
			}
		}
		throw new NoSuchFieldException(name);
	}
}
