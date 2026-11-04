package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.List;

public class InventoryWindow extends EditorWindow {

	private final int page;

	/**
	 * Replaced by the Inventory editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	public static void open(int page) {
		Game.scene().addToFront(new InventoryWindow(page));
	}

	public InventoryWindow(int page) {
		super();
		this.page = page;
		Hero hero = Dungeon.hero;

		addTitle("INVENTORY");
		if (hero == null) {
			finish();
			return;
		}

		addStepper("Gold", String.valueOf(Dungeon.gold), Dungeon.gold, 10, value -> Dungeon.gold = Math.max(0, value));
		addStepper("Energy", String.valueOf(Dungeon.energy), Dungeon.energy, 1, value -> Dungeon.energy = Math.max(0, value));

		List<Item> equipped = InventoryEditor.equipped(hero);
		List<Item> items = new ArrayList<>(equipped);
		items.addAll(InventoryEditor.carried(hero));
		addSection("Items (" + items.size() + ")");

		int perPage = perPage(6, 3);
		int pages = pageCount(items.size(), perPage);
		int current = Math.min(page, pages - 1);
		int end = Math.min(items.size(), (current + 1) * perPage);
		for (int i = current * perPage; i < end; i++) {
			Item item = items.get(i);
			String label = InventoryEditor.titleOf(item);
			if (item.quantity() > 1) label += " x" + item.quantity();
			if (equipped.contains(item)) label += " (equipped)";
			addListRow(label, new ItemSprite(item), equipped.contains(item), true,
				() -> replaceWith(new ItemEditWindow(item, current)),
				() -> showInfo(item));
		}
		addPager(current, pages, newPage -> replaceWith(new InventoryWindow(newPage)));

		float half = (WIDTH - GAP) / 2f;
		addButton("Identify all", 0, half, () -> {
			hero.belongings.identify();
			Item.updateQuickslot();
			refresh();
		});
		addButton("Add item", half + GAP, half, () -> replaceWith(new ItemPickerWindow(InventoryEditor.Category.WEAPONS, 0)));
		pos += ROW_HEIGHT + GAP;

		addHint("Tap an item to edit it. Tap i or long-press to read it.");
		finish();
	}

	static void showInfo(Item item) {
		try {
			show(new WndInfoItem(item));
		} catch (Throwable ignored) {
		}
	}

	@Override
	protected void reopen() {
		open(page);
	}
}
