package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import java.util.List;

public class ItemPickerWindow extends EditorWindow {

	private static int level = 0;
	private static int quantity = 1;
	private static boolean returnToBackpack;
	private static int dropCell = -1;

	private final InventoryEditor.Category category;
	private final int page;

	public ItemPickerWindow(InventoryEditor.Category category, int page) {
		super();
		this.category = category;
		this.page = page;
		Hero hero = Dungeon.hero;

		addTitle(dropCell >= 0 ? "Place item" : "Add item", (dropCell >= 0 ? "Items are placed on the chosen tile. " : "") + "Tap an item to add it to your backpack, identified. If your backpack is full it drops at your feet.\n\nLevel applies to upgradable items and trinkets. Quantity applies to stackable items.\n\nThe heart adds an item to Favorites. Tap i or long-press to read one.");

		InventoryEditor.Category[] categories = InventoryEditor.Category.values();
		int perRow = perPage(3, 5);
		for (int start = 0; start < categories.length; start += perRow) {
			int count = Math.min(perRow, categories.length - start);
			String[] labels = new String[perRow];
			for (int i = 0; i < perRow; i++) labels[i] = i < count ? categories[start + i].label : "";
			int selected = category.ordinal() - start;
			int offset = start;
			addButtonRow(labels, selected, index -> {
				if (offset + index >= categories.length) return;
				replaceWith(new ItemPickerWindow(categories[offset + index], 0));
			});
		}

		addStepper("Level", (level >= 0 ? "+" : "") + level, level, 1, -5, 99, value -> level = Math.max(-5, Math.min(99, value)));
		addStepper("Quantity", String.valueOf(quantity), quantity, 1, 1, 9999, value -> quantity = Math.max(1, Math.min(9999, value)));

		pos += GAP;
		List<InventoryEditor.Entry> entries = InventoryEditor.catalog(category);
		if (entries.isEmpty()) addHint("No favorites yet. Tap the heart next to an item to add it here.");
		addPagedList(entries.size(), perPage(6, 3), page, index -> {
			InventoryEditor.Entry entry = entries.get(index);
			Item sample = sample(entry);
			addListRow(entry.label(), sample == null ? null : new ItemSprite(sample), false, true, () -> {
				InventoryEditor.add(hero, entry.type, level, quantity, dropCell);
				refresh();
			}, sample == null ? null : () -> InventoryWindow.showInfo(sample), new Favorite() {
				@Override
				public boolean isFavorite() {
					return Favorites.isItem(entry.type);
				}

				@Override
				public void toggle() {
					Favorites.toggleItem(entry.type);
				}
			});
		}, newPage -> replaceWith(new ItemPickerWindow(category, newPage)));

		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	/**
	 * Opens the picker to place items on [cell] instead of in the backpack.
	 */
	static void openForCell(int cell) {
		dropCell = cell;
		returnToBackpack = false;
		show(new ItemPickerWindow(InventoryEditor.Category.FAVORITES, 0));
	}

	public static void openFromBackpack() {
		dropCell = -1;
		returnToBackpack = true;
		show(new ItemPickerWindow(InventoryEditor.Category.FAVORITES, 0));
	}

	public static void openFromInventory() {
		dropCell = -1;
		returnToBackpack = false;
		show(new ItemPickerWindow(InventoryEditor.Category.FAVORITES, 0));
	}

	private void back() {
		hide();
		if (dropCell >= 0) {
			int cell = dropCell;
			dropCell = -1;
			TileWindow.open(cell);
		} else if (returnToBackpack) {
			BackpackEditor.open();
		} else {
			InventoryWindow.open();
		}
	}

	private static Item sample(InventoryEditor.Entry entry) {
		try {
			return entry.type.getConstructor().newInstance();
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	protected void reopen() {
		show(new ItemPickerWindow(category, page));
	}
}
