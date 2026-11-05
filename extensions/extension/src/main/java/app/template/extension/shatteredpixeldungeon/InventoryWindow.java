package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.List;

public class InventoryWindow extends EditorWindow {

	/**
	 * Replaced by the Inventory editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	public static void open() {
		Game.scene().addToFront(new InventoryWindow());
	}

	public InventoryWindow() {
		super();
		Hero hero = Dungeon.hero;

		addTitle("INVENTORY", "Open Backpack shows your normal backpack in edit mode, with an Edit button on every item. Back returns here.\n\nAdd Item adds any item in the game. Identify All identifies everything you carry. Cleanse All removes every curse from what you carry, including cursed enchantments and glyphs, like a Scroll of Remove Curse.\n\nThe gear button in the backpack editor brings you back here.\n\nUnlimited inventory space: every bag always has room for one more item. Bags show up to 8 rows of items, and the arrow buttons at the bottom move between pages when there are more.\n\nWeapons, Armor and Rings tabs: extra backpack tabs that show every item of that kind you carry, wherever it is stored. Items stay in their real bags. These tabs also have pages when they hold more than 40 items, and each tab remembers its page.\n\nTap a number to type it, with Min and Max buttons.");
		if (hero == null) {
			addButton("Back", 0, WIDTH, () -> replaceWith(new WndGame()));
			pos += ROW_HEIGHT;
			finish();
			return;
		}

		addStepper("Gold", String.valueOf(Dungeon.gold), Dungeon.gold, 10, 0, 999999, value -> Dungeon.gold = Math.max(0, value));
		addStepper("Energy", String.valueOf(Dungeon.energy), Dungeon.energy, 1, 0, 99999, value -> Dungeon.energy = Math.max(0, value));
		pos += GAP;

		addButton("Open backpack", 0, WIDTH, () -> {
			hide();
			BackpackEditor.open(InventoryWindow::open, Dungeon.hero.belongings.backpack);
		});
		pos += ROW_HEIGHT + GAP;

		float half = (WIDTH - GAP) / 2f;
		addButton("Add item", 0, half, () -> {
			hide();
			ItemPickerWindow.openFromInventory();
		});
		addButton("Identify all", half + GAP, half, () -> {
			hero.belongings.identify();
			Item.updateQuickslot();
			refresh();
		});
		pos += ROW_HEIGHT + GAP;

		addButton("Cleanse all", 0, WIDTH, () -> {
			cleanseAll(hero);
			refresh();
		});
		pos += ROW_HEIGHT + GAP;

		addSection("Backpack");
		addCheckBox("Unlimited inventory space", InventorySpace.isOn(), () -> InventorySpace.setOn(!InventorySpace.isOn()));
		for (int kind = 0; kind < FilterBag.count(); kind++) {
			final int tab = kind;
			addCheckBox(FilterBag.label(kind) + " tab", FilterBag.isOn(kind), () -> FilterBag.setOn(tab, !FilterBag.isOn(tab)));
		}
		pos += GAP;

		addButton("Back", 0, WIDTH, () -> replaceWith(new WndGame()));
		pos += ROW_HEIGHT;
		finish();
	}

	private static void cleanseAll(Hero hero) {
		List<Item> items = new ArrayList<>();
		for (Item item : hero.belongings) items.add(item);
		try {
			ScrollOfRemoveCurse.uncurse(hero, items.toArray(new Item[0]));
		} catch (Throwable e) {
			for (Item item : items) item.cursed = false;
		}
		Item.updateQuickslot();
	}

	static void showInfo(Item item) {
		try {
			show(new WndInfoItem(item));
		} catch (Throwable ignored) {
		}
	}

	@Override
	protected void reopen() {
		open();
	}
}
