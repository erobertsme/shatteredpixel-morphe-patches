package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * The Equipment tab's icon, the size of one item: a smaller armor in the lower left with a smaller sword over it in the upper right.
 */
final class EquipmentIcon extends ItemSprite {

	private static final float PART_SCALE = 0.75f;
	private static final float SHIFT = 1 - PART_SCALE;

	private final ItemSprite armor = new ItemSprite(ItemSpriteSheet.ARMOR_LEATHER);
	private final ItemSprite sword = new ItemSprite(ItemSpriteSheet.SWORD);

	EquipmentIcon() {
		super(ItemSpriteSheet.ARMOR_LEATHER);
	}

	@Override
	public void draw() {
		float width = width();
		float height = height();
		place(armor, x, y + height * SHIFT);
		place(sword, x + width * SHIFT, y);
		armor.draw();
		sword.draw();
	}

	private void place(ItemSprite part, float left, float top) {
		part.camera = camera;
		part.scale.set(scale.x * PART_SCALE, scale.y * PART_SCALE);
		part.x = left;
		part.y = top;
		part.alpha(alpha());
	}
}
