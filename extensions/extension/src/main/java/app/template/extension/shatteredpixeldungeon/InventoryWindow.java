package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.Game;

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

		addTitle("INVENTORY", "Open backpack shows your normal backpack in edit mode, with an Edit button on every item. Back returns here.\n\nAdd item adds any item in the game. Identify all identifies everything you carry.\n\nTap a number to type it, with Min and Max buttons.");
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
			BackpackEditor.open(InventoryWindow::open);
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

		addButton("Back", 0, WIDTH, () -> replaceWith(new WndGame()));
		pos += ROW_HEIGHT;
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
		open();
	}
}
