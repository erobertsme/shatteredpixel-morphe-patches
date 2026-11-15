package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;

/**
 * How much is known about an item: whether it is identified, and whether it is known to be cursed.
 */
enum ItemCondition {
	IDENTIFIED("Identified", 0x66DD66),
	IDENTIFIED_CURSED("Identified, cursed", 0xFF5555),
	UNIDENTIFIED("Unidentified", 0xBBBBBB),
	UNIDENTIFIED_UNCURSED("Unidentified, not cursed", 0x99CCFF),
	UNIDENTIFIED_CURSED("Unidentified, cursed", 0xFF5555);

	final String label;
	final int color;

	ItemCondition(String label, int color) {
		this.label = label;
		this.color = color;
	}

	/**
	 * The condition of [item], or null when there is nothing worth saying, such as for food or a known potion.
	 */
	static ItemCondition of(Item item) {
		if (item == null) return null;
		try {
			boolean curseable = item instanceof EquipableItem || item instanceof Wand;
			if (item.isIdentified()) {
				if (!curseable) return null;
				return item.cursed ? IDENTIFIED_CURSED : IDENTIFIED;
			}
			if (!curseable || !item.cursedKnown) return UNIDENTIFIED;
			return item.cursed ? UNIDENTIFIED_CURSED : UNIDENTIFIED_UNCURSED;
		} catch (Throwable e) {
			return null;
		}
	}
}
