package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;

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
	private static final int RUN = 4;
	private static final int CONTENT_HEIGHT = 150;
	private static final float MAX_SCREEN_SHARE = 0.45f;
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
	 * Called at the end of WndHero.StatsTab.initialize. Moves the rename button beside the [i] button, then replaces the game's stat rows
	 * with tabs and the chosen tab's rows, at the game's column and text size. The rows sit in an area of fixed height that scrolls when
	 * a tab has more, so the window keeps its size whichever tab is shown.
	 */
	public static void afterStatsTab(Object tab) {
		if (!isOn() || !(tab instanceof Group) || Dungeon.hero == null) return;
		try {
			Group group = (Group) tab;
			List<RenderedTextBlock> gameRows = new ArrayList<>();
			List<Component> headerParts = new ArrayList<>();
			for (Gizmo member : Reflect.members(group)) {
				if (member instanceof RenderedTextBlock) gameRows.add((RenderedTextBlock) member);
				if (member instanceof Component && !(member instanceof RenderedTextBlock)) headerParts.add((Component) member);
			}
			if (gameRows.isEmpty()) return;
			float valueX = Float.MAX_VALUE;
			List<Float> labelTops = new ArrayList<>();
			for (RenderedTextBlock row : gameRows) {
				if (row.left() > 1) valueX = Math.min(valueX, row.left());
				if (row.left() <= 1) labelTops.add(row.top());
				group.remove(row);
				row.destroy();
			}
			int width = (Integer) Reflect.getStatic(WndHero.class, "WIDTH");
			if (valueX == Float.MAX_VALUE) valueX = width * 0.6f;
			float top = moveRenameButton(headerParts) + GAP;
			float contentTop = addTabs(group, width, top);
			Component content = new Component();
			float contentHeight = new Layout(content, width, valueX, rowHeight(labelTops)).build(selected, 0);
			content.setSize(width, contentHeight);
			float areaHeight = Math.min(CONTENT_HEIGHT, PixelScene.uiCamera.height * MAX_SCREEN_SHARE);
			if (contentHeight <= areaHeight) {
				content.setPos(0, contentTop);
				group.add(content);
			} else {
				ScrollPane pane = ScrollArea.pane(content);
				group.add(pane);
				pane.setRect(0, contentTop, width, areaHeight);
				ScrollHints.add(group, pane);
			}
			Reflect.set(group.getClass(), group, "pos", contentTop + areaHeight + GAP);
		} catch (Throwable e) {
			Log.e(TAG, "Could not show the hero's stats", e);
		}
	}

	/**
	 * Puts the rename button just left of the [i] button, narrows the title to make room, and returns the bottom of the header.
	 */
	private static float moveRenameButton(List<Component> parts) {
		Component title = null;
		List<Component> buttons = new ArrayList<>();
		for (Component part : parts) {
			if (part instanceof IconButton) {
				buttons.add(part);
			} else if (title == null) {
				title = part;
			}
		}
		buttons.sort((a, b) -> Float.compare(a.top(), b.top()));
		float bottom = title == null ? 0 : title.bottom();
		if (buttons.isEmpty()) return bottom;
		Component info = buttons.get(0);
		float left = info.left();
		for (int i = 1; i < buttons.size(); i++) {
			Component button = buttons.get(i);
			left -= button.width() + GAP;
			button.setPos(left, info.top());
		}
		if (title != null) title.setRect(title.left(), title.top(), left - GAP - title.left(), 0);
		bottom = info.bottom();
		return title == null ? bottom : Math.max(bottom, title.bottom());
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
	 * Builds the stats tab again for the chosen tab. Every tab has the same height, so the window stays as it is.
	 */
	private static void reload(Group tab) {
		try {
			Reflect.call(tab, "initialize", new Class<?>[0]);
		} catch (Throwable e) {
			Log.e(TAG, "Could not switch tabs", e);
		}
	}

	/**
	 * Where and how big the rows go, the way the game lays out its own.
	 */
	private static final class Layout {
		final Group group;
		final int width;
		final float valueX;
		final float rowHeight;

		Layout(Group group, int width, float valueX, float rowHeight) {
			this.group = group;
			this.width = width;
			this.valueX = valueX;
			this.rowHeight = rowHeight;
		}

		/**
		 * Adds tab [tab] from [top] and returns where it ends.
		 */
		float build(int tab, float top) {
			float pos = top;
			for (CharacterWindow.HeroStat row : rows(Dungeon.hero, tab)) {
				addRow(group, row.label, row.value, pos, valueX, width, rowHeight);
				pos += rowHeight;
			}
			if (tab == STATS) return HungerWindow.addCurrentBar(group, 0, pos + GAP, width);
			if (tab == RUN) return addHunger(pos + GAP);
			return pos;
		}

		/**
		 * The hunger section: tapping it opens the hunger settings when the Hunger editor is on, or just the bar without it.
		 */
		private float addHunger(float top) {
			if (!EditorSwitches.hunger()) return HungerWindow.addCurrentBar(group, 0, top, width);
			float bottom = HungerWindow.addSummary(group, 0, top, width);
			Button area = new Button() {
				@Override
				public void onClick() {
					for (Group parent = group.parent; parent != null; parent = parent.parent) {
						if (parent instanceof Window) ((Window) parent).hide();
					}
					HungerWindow.open(CloverlordMenu::showHeroWindow);
				}
			};
			area.setRect(0, top, width, bottom - top);
			group.add(area);
			return bottom;
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
