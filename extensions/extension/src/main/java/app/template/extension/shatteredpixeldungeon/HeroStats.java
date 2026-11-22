package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * All of the hero's stats on the hero info window, in place of its strength, health and experience rows: tabs for the hero's stats,
 * senses, modifiers and resistances, and the run's gold, deepest floor and seed. Laid out like the game's own rows, at the same size.
 */
@SuppressWarnings("unused")
public final class HeroStats {

	private static final String TAG = "HeroStats";
	private static final String[] TABS = { "Stats", "Senses", "Mods", "Resist", "Run" };
	private static final int STATS = 0;
	private static final int SENSES = 1;
	private static final int MODS = 2;
	private static final int RESIST = 3;
	private static final int TEXT_SIZE = 8;
	private static final int TAB_TEXT_SIZE = 6;
	private static final int TAB_HEIGHT = 14;
	private static final int GAP = 2;
	private static final int DEFAULT_ROW = 10;
	private static final HeroExtras.Mod[] OTHER_MODS = {
		HeroExtras.Mod.DAMAGE_DEALT, HeroExtras.Mod.SURPRISE_DAMAGE, HeroExtras.Mod.REGEN, HeroExtras.Mod.HEALING, HeroExtras.Mod.SHIELDING,
		HeroExtras.Mod.DAMAGE_TAKEN, HeroExtras.Mod.TRAP_FALL, HeroExtras.Mod.DEBUFF_TIME, HeroExtras.Mod.BUFF_TIME, HeroExtras.Mod.EXPERIENCE,
		HeroExtras.Mod.GOLD, HeroExtras.Mod.SHOP_DEALS, HeroExtras.Mod.ENERGY, HeroExtras.Mod.THROWN_DURABILITY, HeroExtras.Mod.IDENTIFY_SPEED,
	};

	private static int selected;

	private HeroStats() {
	}

	static boolean isOn() {
		return QolSettings.heroStatsButton();
	}

	/**
	 * Called at the end of WndHero.StatsTab.initialize. Replaces the game's stat rows with tabs and the chosen tab's rows, at the same
	 * place, column and text size.
	 */
	public static void afterStatsTab(Object tab) {
		if (!isOn() || !(tab instanceof Group) || Dungeon.hero == null) return;
		try {
			Group group = (Group) tab;
			List<RenderedTextBlock> gameRows = new ArrayList<>();
			for (Gizmo member : Reflect.members(group)) {
				if (member instanceof RenderedTextBlock) gameRows.add((RenderedTextBlock) member);
			}
			if (gameRows.isEmpty()) return;
			float top = Float.MAX_VALUE;
			float valueX = Float.MAX_VALUE;
			List<Float> labelTops = new ArrayList<>();
			for (RenderedTextBlock row : gameRows) {
				top = Math.min(top, row.top());
				if (row.left() > 1) valueX = Math.min(valueX, row.left());
				if (row.left() <= 1) labelTops.add(row.top());
				row.killAndErase();
				row.destroy();
			}
			float rowHeight = rowHeight(labelTops);
			int width = (Integer) Reflect.getStatic(WndHero.class, "WIDTH");
			if (valueX == Float.MAX_VALUE) valueX = width * 0.6f;
			float pos = top - GAP;
			pos = addTabs(group, width, pos);
			for (CharacterWindow.HeroStat row : rows(Dungeon.hero, selected)) {
				addRow(group, row.label, row.value, pos, valueX, width, rowHeight);
				pos += rowHeight;
			}
			Reflect.set(group.getClass(), group, "pos", pos);
		} catch (Throwable e) {
			Log.e(TAG, "Could not show the hero's stats", e);
		}
	}

	private static float rowHeight(List<Float> tops) {
		tops.sort(Float::compare);
		float step = Float.MAX_VALUE;
		for (int i = 1; i < tops.size(); i++) {
			float difference = tops.get(i) - tops.get(i - 1);
			if (difference > 1) step = Math.min(step, difference);
		}
		return step == Float.MAX_VALUE ? DEFAULT_ROW : step;
	}

	private static float addTabs(Group tab, int width, float top) {
		float buttonWidth = (width - (TABS.length - 1) * GAP) / (float) TABS.length;
		for (int i = 0; i < TABS.length; i++) {
			int index = i;
			RedButton button = new RedButton(TABS[i], TAB_TEXT_SIZE) {
				@Override
				public void onClick() {
					if (index == selected) return;
					selected = index;
					reload(tab);
				}
			};
			if (i == selected) button.textColor(EditorWindow.TITLE_COLOR);
			button.setRect(i * (buttonWidth + GAP), top, buttonWidth, TAB_HEIGHT);
			tab.add(button);
		}
		return top + TAB_HEIGHT + GAP;
	}

	/**
	 * Builds the stats tab again for the chosen tab, and fits the window to it.
	 */
	private static void reload(Group tab) {
		try {
			Reflect.call(tab, "initialize", new Class<?>[0]);
			float height = (Float) Reflect.get(tab.getClass(), tab, "pos");
			for (Group parent = tab.parent; parent != null; parent = parent.parent) {
				if (!(parent instanceof Window)) continue;
				((Window) parent).resize(Reflect.width(parent), (int) height);
				return;
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not switch tabs", e);
		}
	}

	/**
	 * A label and its value, sized down until each fits its column, like the game's own rows.
	 */
	private static void addRow(Group tab, String label, String value, float top, float valueX, int width, float rowHeight) {
		RenderedTextBlock left = fitted(label, valueX - GAP);
		left.setPos(0, top + (rowHeight - left.height()) / 2f);
		PixelScene.align(left);
		tab.add(left);
		RenderedTextBlock right = fitted(value, width - valueX);
		right.setPos(valueX, top + (rowHeight - right.height()) / 2f);
		PixelScene.align(right);
		tab.add(right);
	}

	private static RenderedTextBlock fitted(String text, float width) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, TEXT_SIZE);
		for (int size = TEXT_SIZE - 1; block.width() > width && size >= 4; size--) block = PixelScene.renderTextBlock(text, size);
		return block;
	}

	/**
	 * The rows of tab [tab]: the hero's stats, senses, modifiers, resistances, or the run.
	 */
	static List<CharacterWindow.HeroStat> rows(Hero hero, int tab) {
		List<CharacterWindow.HeroStat> rows = new ArrayList<>();
		if (tab == STATS) {
			rows.add(row("Health", hero.HP + " / " + hero.HT));
			rows.add(row("Experience", hero.exp + " / " + hero.maxExp()));
			rows.addAll(CharacterWindow.heroStats(hero));
			return rows;
		}
		if (tab == SENSES) {
			rows.add(row("Vision", ExtraStats.vision(hero) + " tiles"));
			rows.add(row("Search range", ExtraStats.searchRange(hero) + " tiles"));
			int searchChance = HeroExtras.mod(HeroExtras.Mod.SEARCH_CHANCE);
			rows.add(row("Search chance", searchChance == 0 ? "Normal" : signed(searchChance) + "%"));
			rows.add(row("Stealth", StatsWindow.number(ExtraStats.stealth(hero))));
			rows.add(row("Luck", signed(ExtraStats.ringLevel(hero, HeroExtras.Mod.LUCK))));
			rows.add(row("Enchant power", signed(ExtraStats.ringLevel(hero, HeroExtras.Mod.ENCHANT_POWER))));
			return rows;
		}
		if (tab == MODS) {
			for (HeroExtras.Mod mod : HeroExtras.Mod.in(HeroExtras.Group.RINGS)) {
				int level = ExtraStats.ringLevel(hero, mod);
				if (level != 0) rows.add(row("Ring of " + mod.label, signed(level)));
			}
			for (HeroExtras.Mod mod : OTHER_MODS) {
				int value = HeroExtras.mod(mod);
				if (value != 0) rows.add(row(mod.label, signed(value) + "%"));
			}
			if (rows.isEmpty()) rows.add(row("No modifiers", ""));
			return rows;
		}
		if (tab == RESIST) {
			rows.addAll(ExtraStats.resistances(hero));
			if (rows.isEmpty()) rows.add(row("None", ""));
			return rows;
		}
		rows.add(row("Gold collected", String.valueOf(Statistics.goldCollected)));
		rows.add(row("Maximum depth", String.valueOf(Statistics.deepestFloor)));
		rows.add(row("Seed", DungeonSeed.convertToCode(Dungeon.seed)));
		return rows;
	}

	private static CharacterWindow.HeroStat row(String label, String value) {
		return new CharacterWindow.HeroStat(null, label, value);
	}

	private static String signed(int value) {
		return (value > 0 ? "+" : "") + value;
	}
}
