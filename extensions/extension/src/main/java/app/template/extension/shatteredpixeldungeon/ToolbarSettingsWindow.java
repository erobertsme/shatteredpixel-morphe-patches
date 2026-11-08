package app.template.extension.shatteredpixeldungeon;

public class ToolbarSettingsWindow extends EditorWindow {

	private static final String INFO = "Toolbar\n\nExtra toolbar item slots: more quickslots. Normally they fill a row across the whole width of the screen, below the toolbar, lined up with the quickslots above. With the game's Quickslot Swapper setting on, there is never an extra row: the swap button appears beside your quickslots and swaps to the extra slots instead. Changes show straight away.\n\nAttack Tile button: a button above the backpack button. Tap it, then tap a tile in reach of your weapon, shown in red, to attack it. A creature there is attacked even if it is hiding, so you can strike a mimic before it strikes you, often as a surprise attack. Attacking an empty tile swings at it and takes the time of an attack. With Enchantments affect the tile on, a Blazing, Chilling, Blooming or Shocking weapon rolls its normal chance and, if it triggers, sets the tile alight, freezes it, grows grass there or sends lightning from it. Fire and frost burn or freeze items on the tile and cook or freeze meat. Lightning arcs to creatures within 2 tiles and keeps jumping, further through water, hurting all but your allies for half a hit.\n\nAttack Tile Tag Style sets where the button sits (Auto keeps it next to the game's own tags), its size, colour, icon (Your weapon by default), icon colour, rotation and opacity.";

	public ToolbarSettingsWindow() {
		super();
		addTitle("Toolbar", INFO);
		addCheckBox("Extra toolbar item slots", QuickslotRow.isOn(), () -> QuickslotRow.setOn(!QuickslotRow.isOn()));
		addCheckBox("Attack Tile button", ForceAttack.isOn(), () -> ForceAttack.setOn(!ForceAttack.isOn()));
		addButton("Attack Tile Tag Style", 0, WIDTH, () -> replaceWith(new TagSettingsWindow(ForceAttack.PLACEMENT, "Attack Tile Tag", () -> show(new ToolbarSettingsWindow())))).enable(ForceAttack.isOn());
		pos += ROW_HEIGHT + GAP;
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
		show(new ToolbarSettingsWindow());
	}
}
