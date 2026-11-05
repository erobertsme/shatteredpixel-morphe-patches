package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unlimited inventory space, the Weapons, Armor and Rings backpack tabs, and pages for bags that hold more than
 * fit on screen.
 */
@SuppressWarnings("unused")
public final class InventorySpace {

	private static final String TAG = "InventorySpace";
	private static final String KEY_UNLIMITED = "qol_unlimited_inventory";
	private static final int EQUIPMENT_SLOTS = 5;
	private static final int MAX_ITEM_ROWS = 8;
	private static final int COLUMNS = 5;
	private static final int DEFAULT_ITEM_SLOTS = 20;
	private static final int SLOT_MARGIN = 1;
	private static final int TITLE_HEIGHT = 14;
	private static final int SCREEN_MARGIN = 32;
	private static final int MIN_SLOT_SIZE = 16;
	private static final int ROW_GAP = 2;
	private static final int PAGER_HEIGHT = 16;
	private static final int ARROW_WIDTH = 30;
	private static final int BACK_HEIGHT = 18;

	private static final Map<String, Integer> savedPages = new HashMap<>();

	private static Page current;

	private InventorySpace() {
	}

	private static final class Page {
		Bag bag;
		int fixed;
		int bags;
		int index;
		int pages;
		int free;
		List<Item> shown = new ArrayList<>();
		Set<Item> hidden = Collections.newSetFromMap(new IdentityHashMap<>());
	}

	static boolean isOn() {
		return InventoryWindow.isAvailable() && CloverlordSettings.get(KEY_UNLIMITED);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_UNLIMITED, on);
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
		Page page = current;
		if (page != null && page.bag == bag) return page.bags + page.shown.size() + page.free;
		if (!isOn() || bag == null || bag.items == null) return original;
		return Math.max(original, bag.items.size() + 1);
	}

	static int filterCapacity(FilterBag bag) {
		Page page = current;
		if (page != null && page.bag == bag) return page.shown.size() + page.free - 1;
		return bag.items == null ? 0 : bag.items.size();
	}

	/**
	 * Called with the bag list in the WndBag constructor. Adds the Weapons, Armor and Rings tabs that are switched on.
	 */
	public static ArrayList<Bag> bagTabs(ArrayList<Bag> bags) {
		if (bags == null || !InventoryWindow.isAvailable()) return bags;
		ArrayList<Bag> result = withoutDuplicates(bags);
		for (int kind = 0; kind < FilterBag.count(); kind++) {
			if (FilterBag.isOn(kind)) result.add(FilterBag.get(kind));
		}
		return result;
	}

	/**
	 * Called at the start of Bag.collect. Returns true to refuse a second copy of a bag the hero already carries,
	 * such as a second potion bandolier, so extra copies never add tabs or hold items.
	 */
	public static boolean refuseBag(Bag bag, Bag container) {
		try {
			if (!InventoryWindow.isAvailable() || bag == null || container == null) return false;
			if (bag instanceof Belongings.Backpack || bag instanceof FilterBag) return false;
			Hero hero = Dungeon.hero;
			if (hero == null || container != hero.belongings.backpack || container.items == null) return false;
			for (Item item : container.items) {
				if (item != bag && item.getClass() == bag.getClass()) {
					GLog.w("You already have a " + bag.name() + ". Only one of each bag can be carried.");
					return true;
				}
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not check for a duplicate bag", e);
		}
		return false;
	}

	/**
	 * Keeps the first bag of each kind. Any extra copy already carried hands its items to the first one and is
	 * dropped at the hero's feet.
	 */
	private static ArrayList<Bag> withoutDuplicates(ArrayList<Bag> bags) {
		ArrayList<Bag> result = new ArrayList<>();
		Map<Class<?>, Bag> firsts = new HashMap<>();
		List<Bag> extras = new ArrayList<>();
		for (Bag bag : bags) {
			if (bag instanceof Belongings.Backpack) {
				result.add(bag);
				continue;
			}
			Bag first = firsts.get(bag.getClass());
			if (first == null) {
				firsts.put(bag.getClass(), bag);
				result.add(bag);
				continue;
			}
			extras.add(bag);
		}
		for (Bag extra : extras) dropExtra(extra, firsts.get(extra.getClass()));
		return result;
	}

	private static void dropExtra(Bag extra, Bag first) {
		try {
			Hero hero = Dungeon.hero;
			if (hero == null || Dungeon.level == null) return;
			if (extra.items != null && first != null && first.items != null) {
				first.items.addAll(extra.items);
				extra.items.clear();
			}
			extra.detachAll(hero.belongings.backpack);
			Heap heap = Dungeon.level.drop(extra, hero.pos);
			if (heap != null && heap.sprite != null) heap.sprite.drop();
			GLog.w("You can only carry one " + extra.name() + ". The extra one was dropped.");
			Item.updateQuickslot();
		} catch (Throwable e) {
			Log.e(TAG, "Could not drop an extra bag", e);
		}
	}

	/**
	 * Called at the start of WndBag.icon. Returns the tab icon for the Weapons, Armor and Rings tabs, or null.
	 */
	public static Image tabIcon(Bag bag) {
		if (!(bag instanceof FilterBag)) return null;
		try {
			return new ItemSprite(bag);
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Called in the WndBag constructor once the rows are known. Works out the page to show, and returns the number
	 * of rows to lay out, shrinking the slots so they fit on screen.
	 */
	public static int rows(Object window, Bag bag, int columns, int rows) {
		current = null;
		try {
			if (bag instanceof FilterBag) ((FilterBag) bag).refresh();
			Page page = page(bag);
			if (page == null || columns <= 0) return rows;
			current = page;

			int slots = EQUIPMENT_SLOTS + page.fixed + page.shown.size() + page.free;
			int needed = Math.max(rows, divideUp(slots, columns));
			if (page.pages > 1) needed = Math.max(needed, 1 + MAX_ITEM_ROWS);

			int reserved = (page.pages > 1 ? PAGER_HEIGHT + ROW_GAP : 0) + (BackpackEditor.isOpeningEditWindow() ? BACK_HEIGHT : 0);
			int available = PixelScene.uiCamera.height - SCREEN_MARGIN - TITLE_HEIGHT - reserved - (needed - 1) * SLOT_MARGIN;
			int fit = Math.max(MIN_SLOT_SIZE, available / needed);
			if (fit < getInt(window, "slotHeight")) setInt(window, "slotHeight", fit);
			return needed;
		} catch (Throwable e) {
			Log.e(TAG, "Could not lay out the bag", e);
			current = null;
			return rows;
		}
	}

	private static Page page(Bag bag) {
		Hero hero = Dungeon.hero;
		if (bag == null || bag.items == null || hero == null) return null;
		boolean filter = bag instanceof FilterBag;
		if (!filter && !isOn()) return null;

		Page page = new Page();
		page.bag = bag;
		boolean backpack = bag == hero.belongings.backpack;
		page.fixed = filter ? 0 : backpack ? (hero.belongings.secondWep != null ? 1 : 0) : 1;

		List<Item> all = new ArrayList<>();
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				page.bags++;
				continue;
			}
			all.add(item);
		}

		int perPage = MAX_ITEM_ROWS * COLUMNS - page.fixed;
		int total = all.size() + (filter ? 0 : 1);
		page.pages = Math.max(1, divideUp(total, perPage));
		Integer saved = savedPages.get(key(bag));
		page.index = saved == null ? 0 : Math.max(0, Math.min(page.pages - 1, saved));

		int start = page.index * perPage;
		int end = Math.min(all.size(), start + perPage);
		for (int i = 0; i < all.size(); i++) {
			if (i >= start && i < end) {
				page.shown.add(all.get(i));
			} else {
				page.hidden.add(all.get(i));
			}
		}
		int used = page.fixed + page.shown.size();
		int target;
		if (page.pages > 1) {
			target = MAX_ITEM_ROWS * COLUMNS;
		} else {
			target = Math.max(DEFAULT_ITEM_SLOTS, used + (filter ? 0 : 1));
			target = divideUp(target, COLUMNS) * COLUMNS;
		}
		page.free = Math.max(0, target - used);
		return page;
	}

	private static String key(Bag bag) {
		if (bag instanceof FilterBag) return ((FilterBag) bag).key();
		Hero hero = Dungeon.hero;
		if (hero != null && bag == hero.belongings.backpack) return "backpack";
		return bag.getClass().getName();
	}

	/**
	 * Called at the start of WndBag.placeItem. Returns true to leave out an item that is on another page, or the
	 * Weapons, Armor or Rings tab itself.
	 */
	public static boolean skipSlot(Item item) {
		if (item == null) return false;
		if (item instanceof FilterBag) return true;
		Page page = current;
		return page != null && page.hidden.contains(item);
	}

	/**
	 * Called at the end of the WndBag constructor, before the backpack editor adds its own buttons.
	 */
	static void afterBagCreated(Window window) {
		Page page = current;
		current = null;
		if (page == null || page.pages <= 1) return;
		try {
			addPager(window, page);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the page buttons", e);
		}
	}

	private static void addPager(Window window, Page page) throws ReflectiveOperationException {
		Field widthField = Window.class.getDeclaredField("width");
		Field heightField = Window.class.getDeclaredField("height");
		widthField.setAccessible(true);
		heightField.setAccessible(true);
		int width = widthField.getInt(window);
		int height = heightField.getInt(window);
		float top = height + ROW_GAP;

		RedButton previous = new RedButton("<-", 7) {
			@Override
			public void onClick() {
				turn(window, page, -1);
			}
		};
		previous.setRect(0, top, ARROW_WIDTH, PAGER_HEIGHT);
		window.add(previous);

		RedButton next = new RedButton("->", 7) {
			@Override
			public void onClick() {
				turn(window, page, 1);
			}
		};
		next.setRect(width - ARROW_WIDTH, top, ARROW_WIDTH, PAGER_HEIGHT);
		window.add(next);

		RenderedTextBlock label = PixelScene.renderTextBlock((page.index + 1) + "/" + page.pages, 8);
		label.setPos((width - label.width()) / 2f, top + (PAGER_HEIGHT - label.height()) / 2f);
		window.add(label);

		window.resize(width, (int) (top + PAGER_HEIGHT));
	}

	private static void turn(Window window, Page page, int step) {
		int index = ((page.index + step) % page.pages + page.pages) % page.pages;
		savedPages.put(key(page.bag), index);
		try {
			Field selectorField = WndBag.class.getDeclaredField("selector");
			selectorField.setAccessible(true);
			WndBag.ItemSelector selector = (WndBag.ItemSelector) selectorField.get(window);
			BackpackEditor.beforeTabSwitch(window);
			window.hide();
			WndBag reopened = new WndBag(page.bag, selector);
			if (Game.scene() instanceof GameScene) {
				GameScene.show(reopened);
			} else {
				Game.scene().addToFront(reopened);
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not change the page", e);
		}
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
