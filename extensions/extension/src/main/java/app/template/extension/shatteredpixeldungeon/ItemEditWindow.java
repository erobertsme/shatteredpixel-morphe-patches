package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.Game;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

public class ItemEditWindow extends EditorWindow {

	private static final String[] WEAPON_AUGMENTS = { "Speed", "None", "Damage" };
	private static final Weapon.Augment[] WEAPON_AUGMENT_VALUES = { Weapon.Augment.SPEED, Weapon.Augment.NONE, Weapon.Augment.DAMAGE };
	private static final String[] ARMOR_AUGMENTS = { "Evasion", "None", "Defense" };
	private static final Armor.Augment[] ARMOR_AUGMENT_VALUES = { Armor.Augment.EVASION, Armor.Augment.NONE, Armor.Augment.DEFENSE };

	private final Item item;

	public ItemEditWindow(Item item) {
		super();
		this.item = item;
		Hero hero = Dungeon.hero;

		InfoButton.add(this, WIDTH, pos, "Changes apply straight away. Tap a number to type it, with Min and Max buttons.\n\nInfinite charges keeps a wand or artifact fully charged: it is topped up at the start of every turn, and the setting is saved with the item.\n\nEnchantment or Glyph picks one for weapons or armor, Curses picks a curse. Augment trades speed for damage on weapons, or evasion for defense on armor.\n\nWater sets how full a waterskin is. Blessed blesses an ankh, as if you used a full waterskin on it.\n\nAlchemy turns the item into what alchemy would make from it: potions and scrolls into their exotic versions and back, scrolls into runestones, and seeds into potions.\n\nDuplicate adds a copy to your backpack. Delete removes the item for good, even if it is equipped or cursed.");
		float headerTop = pos;
		addListRow(InventoryEditor.titleOf(item), new ItemSprite(item), true, true, () -> InventoryWindow.showInfo(item), null);
		pos = Math.max(pos, headerTop + InfoButton.SIZE + GAP);

		if (InventoryEditor.isLevelEditable(item)) {
			int level = item.level();
			addStepper("Upgrade level", (level >= 0 ? "+" : "") + level, level, 1, -5, 99, value -> InventoryEditor.setLevel(item, Math.max(-5, Math.min(99, value))));
		}
		if (item.stackable) {
			addStepper("Quantity", String.valueOf(item.quantity()), item.quantity(), 1, 1, 9999, value -> item.quantity(Math.max(1, Math.min(9999, value))));
		}

		addCheckBox("Identified", item.isIdentified(), () -> InventoryEditor.setIdentified(item, !item.isIdentified()));
		addCheckBox("Cursed", item.cursed, () -> InventoryEditor.setCursed(item, !item.cursed));

		if (item instanceof Weapon) {
			Weapon weapon = (Weapon) item;
			addSection("Weapon");
			addPickerRow("Enchantment", weapon.enchantment, false);
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
			addPickerRow("Glyph", armor.glyph, true);
			addSection("Augment");
			addButtonRow(ARMOR_AUGMENTS, indexOf(ARMOR_AUGMENT_VALUES, armor.augment), index -> {
				armor.augment = ARMOR_AUGMENT_VALUES[index];
				Item.updateQuickslot();
				refresh();
			});
		}

		if (item instanceof Wand) {
			Wand wand = (Wand) item;
			addStepper("Charges", wand.curCharges + " / " + wand.maxCharges, wand.curCharges, 1, 0, wand.maxCharges, value -> {
				wand.curCharges = Math.max(0, Math.min(wand.maxCharges, value));
				Item.updateQuickslot();
			});
		}

		if (item instanceof Artifact) {
			Artifact artifact = (Artifact) item;
			int charge = InventoryEditor.artifactValue(artifact, "charge");
			int cap = InventoryEditor.artifactChargeMax(artifact);
			if (cap > 0) addStepper("Charge", charge + " / " + cap, charge, 1, 0, cap, value -> InventoryEditor.setArtifactCharge(artifact, value));
		}

		if (InfiniteCharges.supports(item)) {
			addCheckBox("Infinite charges", InfiniteCharges.isOn(item), () -> InfiniteCharges.setOn(item, !InfiniteCharges.isOn(item)));
		}

		if (ItemAlchemy.isWaterskin(item)) {
			int water = ItemAlchemy.water(item);
			addStepper("Water", water + " / " + ItemAlchemy.WATERSKIN_MAX, water, 1, 0, ItemAlchemy.WATERSKIN_MAX, value -> ItemAlchemy.setWater(item, value));
		}

		if (ItemAlchemy.isAnkh(item)) {
			addCheckBox("Blessed", ItemAlchemy.isBlessed(item), () -> ItemAlchemy.setBlessed(item, !ItemAlchemy.isBlessed(item)));
		}

		java.util.List<Class<? extends Item>> conversions = ItemAlchemy.conversions(item);
		if (!conversions.isEmpty()) {
			addSection("Alchemy");
			for (Class<? extends Item> target : conversions) {
				addButton("Turn into " + ItemAlchemy.name(target), 0, WIDTH, () -> {
					ItemAlchemy.convert(hero, item, target);
					back();
				});
				pos += ROW_HEIGHT + GAP;
			}
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
		}).icon(Icons.get(Icons.CLOSE));
		addButton("Back", 2 * (third + GAP), third, this::back);
		pos += ROW_HEIGHT + GAP;

		finish();
	}

	private void addPickerRow(String label, Object current, boolean armor) {
		boolean curse = InventoryEditor.isCurse(current, armor);
		String text = (curse ? "Curse" : label) + ": " + InventoryEditor.shortEnchantmentName(current);
		addButton(text, 0, WIDTH, () -> replaceWith(new EnchantPickerWindow(item, armor, curse, 0)));
		pos += ROW_HEIGHT + GAP;
	}

	private static <T> int indexOf(T[] values, T value) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == value) return i;
		}
		return -1;
	}

	private static Runnable backAction;
	private static boolean customBack;

	/**
	 * Opens the edit window for [item] from outside the backpack editor. Back runs [back], or just closes when it
	 * is null.
	 */
	static void openFrom(Item item, Runnable back) {
		backAction = back;
		customBack = true;
		Game.scene().addToFront(new ItemEditWindow(item));
	}

	private void back() {
		hide();
		if (!customBack) {
			BackpackEditor.open();
			return;
		}
		Runnable action = backAction;
		backAction = null;
		customBack = false;
		if (action != null) action.run();
	}

	@Override
	protected void reopen() {
		show(new ItemEditWindow(item));
	}
}
