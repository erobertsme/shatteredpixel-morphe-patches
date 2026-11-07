package app.template.extension.shatteredpixeldungeon;

public class BackpackSettingsWindow extends EditorWindow {

	private static final String INFO = "Backpack\n\nRows and pages: Rows of items sets how many rows each page of the backpack and every bag shows, from the game's 4 up to 8, and each extra row is 5 more slots. Extra page for each tab gives every bag a second page of the same size. With unlimited inventory space on, bags show 8 rows and grow as needed instead.\n\nTabs: Weapons, Armor and Rings tabs that hold those items like the potion bandolier holds potions, 19 each, so they don't use backpack space. Needs the Inventory editor patch.";

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
				addCheckBox(FilterBag.label(kind) + " tab", FilterBag.isOn(kind), () -> FilterBag.setOn(tab, !FilterBag.isOn(tab)));
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

	@Override
	protected void reopen() {
		show(new BackpackSettingsWindow());
	}
}
