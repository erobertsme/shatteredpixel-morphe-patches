package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

import java.util.ArrayList;
import java.util.List;

/**
 * The hero's portrait, health, experience, level and status icons at an undo step, laid out like the game's status pane, and the hero's
 * belongings at that step.
 */
final class UndoHeroView {

	static final float HEIGHT = 30;
	private static final float PORTRAIT_WIDTH = 24;
	private static final float PORTRAIT_HEIGHT = 26;
	private static final float BAR_GAP = 3;
	private static final float HP_HEIGHT = 7;
	private static final float XP_HEIGHT = 3;
	private static final int FRAME_COLOR = 0xFF2A2622;
	private static final int HP_BACK = 0xFF3B0D0D;
	private static final int HP_FILL = 0xFFC83232;
	private static final int SHIELD_FILL = 0xFFB4B4B4;
	private static final int XP_BACK = 0xFF2E2A10;
	private static final int XP_FILL = 0xFFFFFF44;
	private static final int LEVEL_COLOR = 0xFFFF44;
	private static final int SLOT_GAP = 1;
	private static final int MAX_SLOT = 20;
	private static final int MIN_SLOT = 12;

	private UndoHeroView() {
	}

	/**
	 * Adds the hero section to [out], [width] wide with its top left corner at [x], [y].
	 */
	static void build(List<Gizmo> out, Hero hero, float x, float y, float width) {
		ColorBlock frame = new ColorBlock(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, FRAME_COLOR);
		frame.x = x;
		frame.y = y;
		out.add(frame);
		try {
			Image avatar = HeroSprite.avatar(hero.heroClass, hero.tier());
			avatar.x = x + (PORTRAIT_WIDTH - avatar.width()) / 2f;
			avatar.y = y + (PORTRAIT_HEIGHT - avatar.height()) / 2f - 2;
			PixelScene.align(avatar);
			out.add(avatar);
		} catch (Throwable e) {
			Log.e("UndoHeroView", "Could not draw the hero", e);
		}
		RenderedTextBlock level = PixelScene.renderTextBlock(String.valueOf(hero.lvl), 6);
		level.hardlight(LEVEL_COLOR);
		level.setPos(x + PORTRAIT_WIDTH - level.width() - 1, y + PORTRAIT_HEIGHT - level.height() - 1);
		PixelScene.align(level);
		out.add(level);
		out.add(new PointerArea(x, y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT) {
			@Override
			public void onClick(PointerEvent event) {
				showTalents(hero);
			}
		});

		float barLeft = x + PORTRAIT_WIDTH + BAR_GAP;
		float barWidth = width - PORTRAIT_WIDTH - BAR_GAP;
		int shield = shielding(hero);
		int max = Math.max(1, hero.HT);
		out.add(block(barLeft, y, barWidth, HP_HEIGHT, HP_BACK));
		float health = barWidth * Math.min(1f, hero.HP / (float) max);
		if (health > 0) out.add(block(barLeft, y, health, HP_HEIGHT, HP_FILL));
		if (shield > 0) {
			float shieldWidth = Math.min(barWidth - health, barWidth * shield / (float) max);
			if (shieldWidth > 0) out.add(block(barLeft + health, y, shieldWidth, HP_HEIGHT, SHIELD_FILL));
		}
		RenderedTextBlock hp = PixelScene.renderTextBlock(hero.HP + "/" + hero.HT + (shield > 0 ? "+" + shield : ""), 6);
		hp.setPos(barLeft + (barWidth - hp.width()) / 2f, y + (HP_HEIGHT - hp.height()) / 2f);
		PixelScene.align(hp);
		out.add(hp);

		float xpTop = y + HP_HEIGHT + 2;
		out.add(block(barLeft, xpTop, barWidth, XP_HEIGHT, XP_BACK));
		int needed = Math.max(1, hero.maxExp());
		float earned = barWidth * Math.min(1f, hero.exp / (float) needed);
		if (earned > 0) out.add(block(barLeft, xpTop, earned, XP_HEIGHT, XP_FILL));
		RenderedTextBlock xp = PixelScene.renderTextBlock("XP " + hero.exp + "/" + needed, 6);
		xp.hardlight(CharacterWindow.DIM_COLOR);
		xp.setPos(barLeft, xpTop + XP_HEIGHT + 1);
		PixelScene.align(xp);
		out.add(xp);

		try {
			BuffIndicator buffs = new BuffIndicator(hero, false);
			buffs.setRect(barLeft, xp.bottom() + 1, barWidth, 8);
			out.add(buffs);
		} catch (Throwable e) {
			Log.e("UndoHeroView", "Could not show the hero's status icons", e);
		}
	}

	private static int shielding(Hero hero) {
		try {
			return hero.shielding();
		} catch (Throwable e) {
			return 0;
		}
	}

	private static ColorBlock block(float x, float y, float width, float height, int color) {
		ColorBlock block = new ColorBlock(width, height, color);
		block.x = x;
		block.y = y;
		return block;
	}

	private static void showTalents(Hero hero) {
		if (hero.talents == null) return;
		Game.scene().addToFront(new TalentsWindow(hero));
	}

	/**
	 * Everything the hero carried: equipped items first, then the backpack, then the contents of each bag.
	 */
	static List<Item> belongings(Hero hero) {
		List<Item> items = new ArrayList<>();
		if (hero.belongings == null) return items;
		for (Item worn : new Item[] { hero.belongings.weapon, hero.belongings.armor, hero.belongings.artifact, hero.belongings.misc, hero.belongings.ring }) {
			if (worn != null) items.add(worn);
		}
		if (hero.belongings.backpack == null) return items;
		List<Bag> bags = new ArrayList<>();
		for (Item item : hero.belongings.backpack.items) {
			items.add(item);
			if (item instanceof Bag) bags.add((Bag) item);
		}
		for (Bag bag : bags) items.addAll(bag.items);
		return items;
	}

	/**
	 * Adds a slot for each item to [out], filling a [width] by [height] area from [x], [y]. Tapping a slot shows the item's details.
	 */
	static void slots(List<Gizmo> out, List<Item> items, float x, float y, float width, float height) {
		if (items.isEmpty()) {
			RenderedTextBlock none = PixelScene.renderTextBlock("Nothing carried", 6);
			none.hardlight(CharacterWindow.DIM_COLOR);
			none.setPos(x, y);
			out.add(none);
			return;
		}
		int size = MAX_SLOT;
		while (size > MIN_SLOT) {
			int columns = Math.max(1, (int) ((width + SLOT_GAP) / (size + SLOT_GAP)));
			int rows = (items.size() + columns - 1) / columns;
			if (rows * (size + SLOT_GAP) <= height) break;
			size--;
		}
		int columns = Math.max(1, (int) ((width + SLOT_GAP) / (size + SLOT_GAP)));
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			ItemSlot slot = new ItemSlot(item) {
				@Override
				public void onClick() {
					Game.scene().addToFront(new WndInfoItem(item));
				}
			};
			slot.setRect(x + (i % columns) * (size + SLOT_GAP), y + (i / columns) * (size + SLOT_GAP), size, size);
			out.add(slot);
		}
	}

	/**
	 * The hero's talents at an undo step. Tapping a talent shows what it does.
	 */
	static final class TalentsWindow extends EditorWindow {

		private static final float SHARE = 0.6f;

		private final Hero hero;

		TalentsWindow(Hero hero) {
			super();
			this.hero = hero;
			addTitle("Talents");
			float height = Math.min(PixelScene.uiCamera.height * SHARE, 200);
			TalentsPane pane = new TalentsPane(TalentButton.Mode.INFO, hero.talents);
			add(pane);
			pane.setRect(0, pos, WIDTH, height);
			pos += height;
			finish();
		}

		@Override
		protected void reopen() {
			show(new TalentsWindow(hero));
		}
	}
}
