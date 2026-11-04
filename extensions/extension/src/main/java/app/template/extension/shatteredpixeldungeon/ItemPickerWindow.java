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

	private final InventoryEditor.Category category;
	private final int page;

	public ItemPickerWindow(InventoryEditor.Category category, int page) {
		super();
		this.category = category;
		this.page = page;
		Hero hero = Dungeon.hero;

		addTitle("Add item");

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
				InventoryEditor.add(hero, entry.type, level, quantity);
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

		addHint("Tap to add (identified). The heart adds it to Favorites. Level applies to upgradable items and trinkets, quantity to stackable ones.");
		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	public static void openFromBackpack() {
		returnToBackpack = true;
		show(new ItemPickerWindow(InventoryEditor.Category.FAVORITES, 0));
	}

	public static void openFromInventory() {
		returnToBackpack = false;
		show(new ItemPickerWindow(InventoryEditor.Category.FAVORITES, 0));
	}

	private void back() {
		hide();
		if (returnToBackpack) {
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
