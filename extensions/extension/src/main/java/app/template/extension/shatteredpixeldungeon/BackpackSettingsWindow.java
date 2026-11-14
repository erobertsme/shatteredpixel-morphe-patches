package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Image;

public class BackpackSettingsWindow extends EditorWindow {

	private static final String INFO = "Backpack\n\nRows and pages: Rows of items sets how many rows each page of the backpack and every bag shows, from the game's 4 up to 8, and each extra row is 5 more slots. Extra page for each tab gives every bag a second page of the same size. With unlimited inventory space on, bags show 8 rows and grow as needed instead.\n\nTabs: Weapons, Armor and Rings tabs that hold those items like the potion bandolier holds potions, 19 each, so they don't use backpack space. Needs the Inventory editor patch.\n\nShopkeeper button beside each tab: when it's red, that tab has to be bought first. Every shop sells a pouch for it for 1000 gold, and once you've bought it from one shop the others stop selling it. When it's grey, the tab is free. Pouches are only sold while the tab is switched on.";

	public BackpackSettingsWindow() {
		super();
		addTitle("Backpack", INFO);
		addHint("Rows of items");
		int count = QolSettings.MAX_BAG_ROWS - QolSettings.DEFAULT_BAG_ROWS + 1;
		String[] labels = new String[count];
		for (int i = 0; i < count; i++) labels[i] = i == 0 ? "Default" : String.valueOf(QolSettings.DEFAULT_BAG_ROWS + i);
		addButtonRow(labels, QolSettings.bagRows() - QolSettings.DEFAULT_BAG_ROWS, index -> {
			QolSettings.bagRows(QolSettings.DEFAULT_BAG_ROWS + index);
			refresh();
		});
		addCheckBox("Extra page for each tab", QolSettings.extraBagPage(), () -> QolSettings.extraBagPage(!QolSettings.extraBagPage()));
		if (InventoryWindow.isAvailable()) {
			pos += GAP;
			addSection("Tabs");
			for (int kind = 0; kind < FilterBag.count(); kind++) {
				final int tab = kind;
				checkBoxSideButton(shopToggle(tab));
				addCheckBox(FilterBag.label(kind) + " tab", FilterBag.enabled(kind), () -> FilterBag.setOn(tab, !FilterBag.enabled(tab)));
			}
		}
		pos += GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private static final String SHOPKEEPER = "sprites/shopkeeper.png";
	private static final int SHOPKEEPER_SIZE = 14;

	/**
	 * The shopkeeper button beside a tab: red with the shopkeeper solid when the tab has to be bought from a shop,
	 * grey with the shopkeeper faded when it doesn't.
	 */
	private StyledButton shopToggle(int tab) {
		boolean on = TabPouches.soldInShops(tab);
		StyledButton button = new StyledButton(on ? Chrome.Type.RED_BUTTON : Chrome.Type.GREY_BUTTON, "") {
			@Override
			public void onClick() {
				TabPouches.soldInShops(tab, !TabPouches.soldInShops(tab));
				refresh();
			}
		};
		Image keeper = new Image(SHOPKEEPER, SHOPKEEPER_SIZE, 0, SHOPKEEPER_SIZE, SHOPKEEPER_SIZE);
		keeper.alpha(on ? 1f : 0.45f);
		button.icon(keeper);
		return button;
	}

	@Override
	protected void reopen() {
		show(new BackpackSettingsWindow());
	}
}
