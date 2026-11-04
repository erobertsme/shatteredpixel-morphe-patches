package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import java.util.List;

public class ItemPickerWindow extends EditorWindow {

	private static int level = 0;
	private static int quantity = 1;

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

		addStepper("Level", (level >= 0 ? "+" : "") + level, level, 1, value -> level = Math.max(-5, Math.min(99, value)));
		addStepper("Quantity", String.valueOf(quantity), quantity, 1, value -> quantity = Math.max(1, Math.min(9999, value)));

		List<InventoryEditor.Entry> entries = InventoryEditor.catalog(category);
		int perPage = perPage(6, 3);
		int pages = pageCount(entries.size(), perPage);
		int shown = Math.min(page, pages - 1);
		int end = Math.min(entries.size(), (shown + 1) * perPage);
		for (int i = shown * perPage; i < end; i++) {
			InventoryEditor.Entry entry = entries.get(i);
			Item sample = sample(entry);
			addListRow(entry.label(), sample == null ? null : new ItemSprite(sample), false, true, () -> {
				InventoryEditor.add(hero, entry.type, level, quantity);
				refresh();
			}, sample == null ? null : () -> InventoryWindow.showInfo(sample));
		}
		addPager(shown, pages, newPage -> replaceWith(new ItemPickerWindow(category, newPage)));

		addHint("Tap to add (identified). Level applies to upgradable items and trinkets, quantity to stackable ones. A full backpack drops it at your feet.");
		addButton("Back", 0, WIDTH, () -> replaceWith(new InventoryWindow(0)));
		pos += ROW_HEIGHT;
		finish();
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
