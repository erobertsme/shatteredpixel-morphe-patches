package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;

import java.util.List;

public class EnchantPickerWindow extends EditorWindow {

	private final Item item;
	private final int inventoryPage;
	private final boolean armor;
	private final boolean curses;
	private final int page;

	public EnchantPickerWindow(Item item, int inventoryPage, boolean armor, boolean curses, int page) {
		super();
		this.item = item;
		this.inventoryPage = inventoryPage;
		this.armor = armor;
		this.curses = curses;
		this.page = page;

		addTitle((armor ? "Glyph for " : "Enchant ") + InventoryEditor.titleOf(item));
		addButtonRow(new String[] { armor ? "Glyphs" : "Enchantments", "Curses" }, curses ? 1 : 0,
			index -> replaceWith(new EnchantPickerWindow(item, inventoryPage, armor, index == 1, 0)));

		Object current = armor ? ((Armor) item).glyph : ((Weapon) item).enchantment;
		if (!curses) addListRow("None", null, current == null, true, () -> apply(null), null);

		List<Object> options = InventoryEditor.enchantments(armor, curses);
		int perPage = perPage(6, 3);
		int pages = pageCount(options.size(), perPage);
		int shown = Math.min(page, pages - 1);
		int end = Math.min(options.size(), (shown + 1) * perPage);
		for (int i = shown * perPage; i < end; i++) {
			Object option = options.get(i);
			boolean selected = current != null && current.getClass() == option.getClass();
			addListRow(InventoryEditor.enchantmentName(option), null, selected, true, () -> apply(option), () -> showInfo(option));
		}
		addPager(shown, pages, newPage -> replaceWith(new EnchantPickerWindow(item, inventoryPage, armor, curses, newPage)));

		addHint("Tap to apply. Tap i or long-press to read it.");
		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private void apply(Object option) {
		InventoryEditor.applyEnchantment(item, option);
		back();
	}

	private static void showInfo(Object option) {
		try {
			show(new WndMessage(InventoryEditor.enchantmentName(option) + "\n\n" + InventoryEditor.enchantmentDesc(option)));
		} catch (Throwable ignored) {
		}
	}

	private void back() {
		replaceWith(new ItemEditWindow(item, inventoryPage));
	}

	@Override
	protected void reopen() {
		show(new EnchantPickerWindow(item, inventoryPage, armor, curses, page));
	}
}
