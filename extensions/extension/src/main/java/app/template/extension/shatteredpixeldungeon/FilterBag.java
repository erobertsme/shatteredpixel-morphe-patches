package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;
import java.util.List;

/**
 * A backpack tab that shows every carried item of one kind. It only exists while a bag window shows it: it is
 * never added to the hero's belongings, so items always stay in their real bags and saves never contain it.
 */
final class FilterBag extends Bag {

	static final int WEAPONS = 0;
	static final int ARMOR = 1;
	static final int RINGS = 2;

	private static final String[] NAMES = { "Weapons", "Armor", "Rings" };
	private static final String[] KEYS = { "inv_tab_weapons_off", "inv_tab_armor_off", "inv_tab_rings_off" };
	private static final FilterBag[] TABS = new FilterBag[NAMES.length];

	private final int kind;

	private FilterBag(int kind) {
		this.kind = kind;
		this.image = icon(kind);
		this.items = new ArrayList<>();
	}

	static synchronized FilterBag get(int kind) {
		if (TABS[kind] == null) TABS[kind] = new FilterBag(kind);
		return TABS[kind];
	}

	static String label(int kind) {
		return NAMES[kind];
	}

	static boolean isOn(int kind) {
		return InventoryWindow.isAvailable() && !CloverlordSettings.get(KEYS[kind]);
	}

	static void setOn(int kind, boolean on) {
		CloverlordSettings.set(KEYS[kind], !on);
	}

	static int count() {
		return NAMES.length;
	}

	private static int icon(int kind) {
		try {
			if (kind == WEAPONS) return ItemSpriteSheet.SWORD;
			if (kind == ARMOR) return ItemSpriteSheet.ARMOR_LEATHER;
			return ItemSpriteSheet.RING_GARNET;
		} catch (Throwable e) {
			return 0;
		}
	}

	String key() {
		return "filter_" + kind;
	}

	/**
	 * Fills the tab with the matching items from the backpack and every bag inside it.
	 */
	void refresh() {
		List<Item> found = new ArrayList<>();
		if (Dungeon.hero != null && Dungeon.hero.belongings != null) collect(Dungeon.hero.belongings.backpack, found);
		items = new ArrayList<>(found);
	}

	private void collect(Bag bag, List<Item> found) {
		if (bag == null || bag.items == null) return;
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				collect((Bag) item, found);
				continue;
			}
			if (accepts(item)) found.add(item);
		}
	}

	private boolean accepts(Item item) {
		if (kind == WEAPONS) return item instanceof Weapon;
		if (kind == ARMOR) return item instanceof Armor;
		return item instanceof Ring;
	}

	@Override
	public String name() {
		return NAMES[kind];
	}

	@Override
	public String desc() {
		return "Every " + NAMES[kind].toLowerCase() + " item you are carrying, wherever it is stored.";
	}

	@Override
	public String info() {
		return desc();
	}

	@Override
	public int capacity() {
		return InventorySpace.filterCapacity(this);
	}
}
