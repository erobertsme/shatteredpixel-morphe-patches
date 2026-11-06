package app.template.extension.shatteredpixeldungeon;

public class BagToolbarWindow extends EditorWindow {

	private static final String INFO = "Backpack & Toolbar\n\nBackpack: Rows of items sets how many rows each page of the backpack and every bag shows, from the game's 4 up to 8, and each extra row is 5 more slots. Extra page for each tab gives every bag a second page of the same size. With unlimited inventory space on, bags show 8 rows and grow as needed instead.\n\nBackpack tabs: Weapons, Armor and Rings tabs that hold those items like the potion bandolier holds potions, 19 each, so they don't use backpack space. Needs the Inventory editor patch.\n\nExtra toolbar item slots: more quickslots. Normally they fill a row across the whole width of the screen, below the toolbar, lined up with the quickslots above. With the game's Quickslot Swapper setting on, there is never an extra row: the swap button appears beside your quickslots and swaps to the extra slots instead. Changes show straight away.\n\nAttack Tile button: a button above the backpack button. Tap it, then tap a tile in reach of your weapon, shown in red, to attack it. A creature there is attacked even if it is hiding, so you can strike a mimic before it strikes you, often as a surprise attack. Attacking an empty tile swings at it and takes the time of an attack. With Enchantments affect the tile on, a Blazing, Chilling, Blooming or Shocking weapon rolls its normal chance and, if it triggers, sets the tile alight, freezes it, grows grass there or sends lightning from it. Fire and frost burn or freeze items on the tile and cook or freeze meat. Lightning arcs to creatures within 2 tiles and keeps jumping, further through water, hurting all but your allies for half a hit.";

	public BagToolbarWindow() {
		super();
		addTitle("Backpack & Toolbar", INFO);

		addSection("Backpack");
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
			for (int kind = 0; kind < FilterBag.count(); kind++) {
				final int tab = kind;
				addCheckBox(FilterBag.label(kind) + " tab", FilterBag.isOn(kind), () -> FilterBag.setOn(tab, !FilterBag.isOn(tab)));
			}
		}

		pos += GAP;
		addSection("Toolbar");
		addCheckBox("Extra toolbar item slots", QuickslotRow.isOn(), () -> QuickslotRow.setOn(!QuickslotRow.isOn()));
		addCheckBox("Attack Tile button", ForceAttack.isOn(), () -> ForceAttack.setOn(!ForceAttack.isOn()));
		addCheckBox("Enchantments affect the tile", QolSettings.forceAttackEnchants(), () -> QolSettings.forceAttackEnchants(!QolSettings.forceAttackEnchants()), ForceAttack.isOn());

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
		show(new BagToolbarWindow());
	}
}
