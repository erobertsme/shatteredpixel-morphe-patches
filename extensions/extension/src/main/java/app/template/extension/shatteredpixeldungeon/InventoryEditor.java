package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

final class InventoryEditor {

	private static final String TAG = "InventoryEditor";

	enum Category {
		FAVORITES("Favorites"),
		WEAPONS("Weapons"),
		ARMOR("Armor"),
		WANDS("Wands"),
		RINGS("Rings"),
		ARTIFACTS("Artifacts"),
		TRINKETS("Trinkets"),
		POTIONS("Potions"),
		SCROLLS("Scrolls"),
		QUEST("Quest"),
		OTHER("Other");

		final String label;

		Category(String label) {
			this.label = label;
		}
	}

	static final class Entry {
		final Class<? extends Item> type;
		final String name;
		final Category category;
		final int tier;

		Entry(Class<? extends Item> type, String name, Category category, int tier) {
			this.type = type;
			this.name = name;
			this.category = category;
			this.tier = tier;
		}

		String label() {
			return tier > 0 ? "T" + tier + " " + name : name;
		}
	}

	private static List<Entry> catalog;

	private InventoryEditor() {
	}

	static synchronized List<Entry> catalog() {
		if (catalog != null) return catalog;
		Map<Class<?>, Entry> loaded = new HashMap<>();
		for (String className : ItemClasses.NAMES) {
			Entry entry = load(className);
			if (entry != null) loaded.put(entry.type, entry);
		}

		List<Entry> entries = new ArrayList<>();
		for (Entry entry : loaded.values()) {
			if (!inheritsName(entry, loaded)) entries.add(entry);
		}
		Collections.sort(entries, (a, b) -> {
			if (a.tier != b.tier) return Integer.compare(a.tier, b.tier);
			boolean aMissile = MissileWeapon.class.isAssignableFrom(a.type);
			boolean bMissile = MissileWeapon.class.isAssignableFrom(b.type);
			if (aMissile != bMissile) return aMissile ? 1 : -1;
			return a.name.compareToIgnoreCase(b.name);
		});
		catalog = entries;
		return catalog;
	}

	static List<Entry> catalog(Category category) {
		List<Entry> result = new ArrayList<>();
		for (Entry entry : catalog()) {
			boolean matches = category == Category.FAVORITES ? Favorites.isItem(entry.type) : entry.category == category;
			if (matches) result.add(entry);
		}
		return result;
	}

	private static boolean inheritsName(Entry entry, Map<Class<?>, Entry> loaded) {
		for (Class<?> parent = entry.type.getSuperclass(); parent != null && parent != Item.class; parent = parent.getSuperclass()) {
			Entry parentEntry = loaded.get(parent);
			if (parentEntry != null && parentEntry.name.equalsIgnoreCase(entry.name)) return true;
		}
		return false;
	}

	private static int tierOf(Item item) {
		if (item instanceof MeleeWeapon) return ((MeleeWeapon) item).tier;
		if (item instanceof MissileWeapon) return ((MissileWeapon) item).tier;
		return 0;
	}

	@SuppressWarnings("unchecked")
	private static Entry load(String className) {
		try {
			Class<?> type = Class.forName(className);
			if (!Item.class.isAssignableFrom(type)) return null;
			Item sample = (Item) type.getConstructor().newInstance();
			String name = sample.trueName();
			if (name == null || name.isEmpty() || name.startsWith("!!!")) return null;
			return new Entry((Class<? extends Item>) type, capitalize(name), categorize(className, sample), tierOf(sample));
		} catch (Throwable e) {
			return null;
		}
	}

	private static Category categorize(String className, Item item) {
		if (className.contains(".items.quest.") || className.contains(".items.keys.") || className.endsWith(".items.Amulet")) return Category.QUEST;
		if (item instanceof KindOfWeapon) return Category.WEAPONS;
		if (item instanceof Armor) return Category.ARMOR;
		if (item instanceof Wand) return Category.WANDS;
		if (item instanceof Ring) return Category.RINGS;
		if (item instanceof Artifact) return Category.ARTIFACTS;
		if (item instanceof Trinket) return Category.TRINKETS;
		if (className.contains(".items.potions.")) return Category.POTIONS;
		if (className.contains(".items.scrolls.")) return Category.SCROLLS;
		return Category.OTHER;
	}

	static List<Item> equipped(Hero hero) {
		Belongings belongings = hero.belongings;
		List<Item> result = new ArrayList<>();
		if (belongings.weapon != null) result.add(belongings.weapon);
		if (belongings.armor != null) result.add(belongings.armor);
		if (belongings.artifact != null) result.add(belongings.artifact);
		if (belongings.misc != null) result.add(belongings.misc);
		if (belongings.ring != null) result.add(belongings.ring);
		return result;
	}

	static boolean isEquipped(Hero hero, Item item) {
		return equipped(hero).contains(item);
	}

	static void add(Hero hero, Class<? extends Item> type, int level, int quantity) {
		add(hero, type, level, quantity, -1);
	}

	/**
	 * Adds an item to the backpack, or drops it on [cell] when [cell] is not -1.
	 */
	static void add(Hero hero, Class<? extends Item> type, int level, int quantity, int cell) {
		try {
			Item item = type.getConstructor().newInstance();
			if (level != 0 && isLevelEditable(item)) setLevel(item, level);
			if (item.stackable) item.quantity(Math.max(1, quantity));
			item.identify();
			if (cell >= 0) {
				Heap heap = FloorEditor.dropWithoutTrigger(item, cell, ItemPickerWindow.keepTrapActive());
				if (heap != null && heap.sprite != null) heap.sprite.drop();
				return;
			}
			give(hero, item);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add " + type.getName(), e);
		}
	}

	static void duplicate(Hero hero, Item item) {
		try {
			give(hero, item.duplicate());
		} catch (Throwable e) {
			Log.e(TAG, "Could not duplicate " + item.getClass().getName(), e);
		}
	}

	private static void give(Hero hero, Item item) {
		if (!item.collect(hero.belongings.backpack)) {
			Heap heap = Dungeon.level.drop(item, hero.pos);
			if (heap != null && heap.sprite != null) heap.sprite.drop();
		}
		Item.updateQuickslot();
	}

	static void delete(Hero hero, Item item) {
		try {
			if (isEquipped(hero, item) && item instanceof EquipableItem) {
				boolean cursed = item.cursed;
				item.cursed = false;
				((EquipableItem) item).doUnequip(hero, false, false);
				item.cursed = cursed;
			} else {
				item.detachAll(hero.belongings.backpack);
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not delete " + item.getClass().getName(), e);
		}
		Item.updateQuickslot();
	}

	static boolean isLevelEditable(Item item) {
		return item.isUpgradable() || item instanceof Wand || item instanceof Artifact || item instanceof Trinket;
	}

	static void setLevel(Item item, int level) {
		item.level(level);
		if (item instanceof Wand) {
			Wand wand = (Wand) item;
			wand.updateLevel();
			wand.curCharges = Math.min(wand.curCharges, wand.maxCharges);
		}
		Item.updateQuickslot();
	}

	static void setIdentified(Item item, boolean identified) {
		if (identified) {
			item.identify();
		} else {
			item.levelKnown = false;
			item.cursedKnown = false;
		}
		Item.updateQuickslot();
	}

	static void setCursed(Item item, boolean cursed) {
		item.cursed = cursed;
		item.cursedKnown = true;
		Item.updateQuickslot();
	}

	static int artifactValue(Artifact artifact, String field) {
		try {
			Field f = Reflect.field(Artifact.class, field);
			return f.getInt(artifact);
		} catch (Throwable e) {
			return 0;
		}
	}

	/**
	 * The most charge [artifact] can hold. Most artifacts have a fixed cap. Ethereal chains recharge up to twice
	 * their normal amount, and the alchemist's toolkit stores energy without a limit.
	 */
	static int artifactChargeMax(Artifact artifact) {
		int cap = artifactValue(artifact, "chargeCap");
		if (cap > 0) return cap;
		String name = artifact.getClass().getSimpleName();
		if (name.equals("EtherealChains")) return 2 * (5 + 2 * artifact.level());
		if (name.equals("AlchemistsToolkit")) return Math.max(TOOLKIT_EDIT_MAX, artifactValue(artifact, "charge"));
		return 0;
	}

	private static final int TOOLKIT_EDIT_MAX = 999;

	static void setArtifactCharge(Artifact artifact, int value) {
		try {
			Field f = Reflect.field(Artifact.class, "charge");
			f.setInt(artifact, Math.max(0, Math.min(artifactChargeMax(artifact), value)));
		} catch (Throwable e) {
			Log.e(TAG, "Could not set artifact charge", e);
		}
		Item.updateQuickslot();
	}

	static List<Object> enchantments(boolean armor, boolean curses) {
		List<Object> result = new ArrayList<>();
		Class<?>[][] groups = armor
			? (curses ? new Class<?>[][] { Armor.Glyph.curses } : new Class<?>[][] { Armor.Glyph.common, Armor.Glyph.uncommon, Armor.Glyph.rare })
			: (curses ? new Class<?>[][] { Weapon.Enchantment.curses } : new Class<?>[][] { Weapon.Enchantment.common, Weapon.Enchantment.uncommon, Weapon.Enchantment.rare });
		for (Class<?>[] group : groups) {
			if (group == null) continue;
			for (Class<?> type : group) {
				try {
					result.add(type.getConstructor().newInstance());
				} catch (Throwable ignored) {
				}
			}
		}
		return result;
	}

	static String enchantmentName(Object enchantment) {
		if (enchantment == null) return "None";
		try {
			if (enchantment instanceof Weapon.Enchantment) return capitalize(((Weapon.Enchantment) enchantment).name());
			if (enchantment instanceof Armor.Glyph) return capitalize(((Armor.Glyph) enchantment).name());
		} catch (Throwable ignored) {
		}
		return enchantment.getClass().getSimpleName();
	}

	/**
	 * The enchantment, glyph or curse name without the word enchantment, glyph or curse, such as Blazing.
	 */
	static String shortEnchantmentName(Object enchantment) {
		if (enchantment == null) return "None";
		try {
			String name = null;
			if (enchantment instanceof Weapon.Enchantment) name = ((Weapon.Enchantment) enchantment).name("");
			if (enchantment instanceof Armor.Glyph) name = ((Armor.Glyph) enchantment).name("");
			if (name != null) {
				name = name.trim();
				if (name.startsWith("of ")) name = name.substring(3).trim();
				if (!name.isEmpty()) return capitalize(name);
			}
		} catch (Throwable ignored) {
		}
		return enchantmentName(enchantment);
	}

	static boolean isCurse(Object enchantment, boolean armor) {
		if (enchantment == null) return false;
		for (Object curse : enchantments(armor, true)) {
			if (curse.getClass() == enchantment.getClass()) return true;
		}
		return false;
	}

	static String enchantmentDesc(Object enchantment) {
		try {
			if (enchantment instanceof Weapon.Enchantment) return ((Weapon.Enchantment) enchantment).desc();
			if (enchantment instanceof Armor.Glyph) return ((Armor.Glyph) enchantment).desc();
		} catch (Throwable ignored) {
		}
		return "";
	}

	static void applyEnchantment(Item item, Object enchantment) {
		if (item instanceof Weapon) ((Weapon) item).enchant((Weapon.Enchantment) enchantment);
		if (item instanceof Armor) ((Armor) item).inscribe((Armor.Glyph) enchantment);
		Item.updateQuickslot();
	}

	static String titleOf(Item item) {
		try {
			return capitalize(item.title());
		} catch (Throwable e) {
			return item.getClass().getSimpleName();
		}
	}

	static String capitalize(String text) {
		if (text == null || text.isEmpty()) return "";
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}
}
