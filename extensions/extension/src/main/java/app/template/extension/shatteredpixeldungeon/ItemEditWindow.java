package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class ItemEditWindow extends EditorWindow {

	private static final String[] WEAPON_AUGMENTS = { "Speed", "None", "Damage" };
	private static final Weapon.Augment[] WEAPON_AUGMENT_VALUES = { Weapon.Augment.SPEED, Weapon.Augment.NONE, Weapon.Augment.DAMAGE };
	private static final String[] ARMOR_AUGMENTS = { "Evasion", "None", "Defense" };
	private static final Armor.Augment[] ARMOR_AUGMENT_VALUES = { Armor.Augment.EVASION, Armor.Augment.NONE, Armor.Augment.DEFENSE };

	private final Item item;
	private final int inventoryPage;

	public ItemEditWindow(Item item, int inventoryPage) {
		super();
		this.item = item;
		this.inventoryPage = inventoryPage;
		Hero hero = Dungeon.hero;

		addListRow(InventoryEditor.titleOf(item), new ItemSprite(item), true, true, () -> InventoryWindow.showInfo(item), () -> InventoryWindow.showInfo(item));

		if (InventoryEditor.isLevelEditable(item)) {
			int level = item.level();
			addStepper("Upgrade level", (level >= 0 ? "+" : "") + level, level, 1, value -> InventoryEditor.setLevel(item, Math.max(-5, Math.min(99, value))));
		}
		if (item.stackable) {
			addStepper("Quantity", String.valueOf(item.quantity()), item.quantity(), 1, value -> item.quantity(Math.max(1, Math.min(9999, value))));
		}

		addCheckBox("Identified", item.isIdentified(), () -> InventoryEditor.setIdentified(item, !item.isIdentified()));
		addCheckBox("Cursed", item.cursed, () -> InventoryEditor.setCursed(item, !item.cursed));

		if (item instanceof Weapon) {
			Weapon weapon = (Weapon) item;
			addSection("Weapon");
			addPickerRow("Enchantment", InventoryEditor.enchantmentName(weapon.enchantment), false);
			addSection("Augment");
			addButtonRow(WEAPON_AUGMENTS, indexOf(WEAPON_AUGMENT_VALUES, weapon.augment), index -> {
				weapon.augment = WEAPON_AUGMENT_VALUES[index];
				Item.updateQuickslot();
				refresh();
			});
		}

		if (item instanceof Armor) {
			Armor armor = (Armor) item;
			addSection("Armor");
			addPickerRow("Glyph", InventoryEditor.enchantmentName(armor.glyph), true);
			addSection("Augment");
			addButtonRow(ARMOR_AUGMENTS, indexOf(ARMOR_AUGMENT_VALUES, armor.augment), index -> {
				armor.augment = ARMOR_AUGMENT_VALUES[index];
				Item.updateQuickslot();
				refresh();
			});
		}

		if (item instanceof Wand) {
			Wand wand = (Wand) item;
			addStepper("Charges", wand.curCharges + " / " + wand.maxCharges, wand.curCharges, 1, value -> {
				wand.curCharges = Math.max(0, Math.min(wand.maxCharges, value));
				Item.updateQuickslot();
			});
		}

		if (item instanceof Artifact) {
			Artifact artifact = (Artifact) item;
			int charge = InventoryEditor.artifactValue(artifact, "charge");
			int cap = InventoryEditor.artifactValue(artifact, "chargeCap");
			if (cap > 0) addStepper("Charge", charge + " / " + cap, charge, 1, value -> InventoryEditor.setArtifactCharge(artifact, value));
		}

		pos += GAP;
		float third = (WIDTH - 2 * GAP) / 3f;
		addButton("Duplicate", 0, third, () -> {
			InventoryEditor.duplicate(hero, item);
			back();
		});
		addButton("Delete", third + GAP, third, () -> {
			InventoryEditor.delete(hero, item);
			back();
		}).textColor(CharacterWindow.NEGATIVE_COLOR);
		addButton("Back", 2 * (third + GAP), third, this::back);
		pos += ROW_HEIGHT + GAP;

		finish();
	}

	private void addPickerRow(String label, String current, boolean armor) {
		float half = (WIDTH - GAP) / 2f;
		addButton(label + ": " + current, 0, half, () -> replaceWith(new EnchantPickerWindow(item, inventoryPage, armor, false, 0)));
		addButton("Curses", half + GAP, half, () -> replaceWith(new EnchantPickerWindow(item, inventoryPage, armor, true, 0)));
		pos += ROW_HEIGHT + GAP;
	}

	private static <T> int indexOf(T[] values, T value) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == value) return i;
		}
		return -1;
	}

	private void back() {
		replaceWith(new InventoryWindow(inventoryPage));
	}

	@Override
	protected void reopen() {
		show(new ItemEditWindow(item, inventoryPage));
	}
}
