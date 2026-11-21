package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;

import java.util.Locale;

/**
 * The hero stats popup, opened from the stats button on the hero info window.
 */
@SuppressWarnings("unused")
public class HeroStatsWindow extends EditorWindow {

	private static final int BUTTON_SIZE = 16;
	private static final HeroExtras.Mod[] OTHER_MODS = {
		HeroExtras.Mod.DAMAGE_DEALT, HeroExtras.Mod.SURPRISE_DAMAGE, HeroExtras.Mod.REGEN, HeroExtras.Mod.HEALING, HeroExtras.Mod.SHIELDING,
		HeroExtras.Mod.DAMAGE_TAKEN, HeroExtras.Mod.TRAP_FALL, HeroExtras.Mod.DEBUFF_TIME, HeroExtras.Mod.BUFF_TIME, HeroExtras.Mod.EXPERIENCE,
		HeroExtras.Mod.GOLD, HeroExtras.Mod.SHOP_DEALS, HeroExtras.Mod.ENERGY, HeroExtras.Mod.THROWN_DURABILITY, HeroExtras.Mod.IDENTIFY_SPEED,
	};

	static boolean isOn() {
		return QolSettings.heroStatsButton();
	}

	/**
	 * Called at the end of WndHero.StatsTab.initialize. Adds the stats button below the [i] and rename buttons.
	 */
	@SuppressWarnings("unchecked")
	public static void afterStatsTab(Object tab) {
		if (!isOn() || !(tab instanceof Group) || Dungeon.hero == null) return;
		try {
			float left = -1;
			float bottom = 0;
			for (Gizmo member : Reflect.members(tab)) {
				if (!(member instanceof IconButton)) continue;
				Component button = (Component) member;
				if (button.top() > BUTTON_SIZE * 2) continue;
				left = button.left();
				bottom = Math.max(bottom, button.bottom());
			}
			if (left < 0) return;
			IconButton stats = new IconButton(Icons.get(Icons.STATS)) {
				@Override
				public void onClick() {
					Game.scene().addToFront(new HeroStatsWindow());
				}
			};
			stats.setRect(left, bottom, BUTTON_SIZE, BUTTON_SIZE);
			((Group) tab).add(stats);
		} catch (Throwable e) {
			android.util.Log.e("HeroStatsWindow", "Could not add the stats button", e);
		}
	}

	public HeroStatsWindow() {
		super();
		Hero hero = Dungeon.hero;
		addTitle(("Level " + hero.lvl + " " + hero.className()).toUpperCase(Locale.ENGLISH));
		addIconRow(StatIcons.item(StatIcons.POTION_HEALING), "Health", hero.HP + " / " + hero.HT);
		addIconRow(StatIcons.item(StatIcons.POTION_EXP), "Experience", hero.exp + " / " + hero.maxExp());
		for (CharacterWindow.HeroStat stat : CharacterWindow.heroStats(hero)) addIconRow(stat.icon, stat.label, stat.value);

		addSection("Senses & luck");
		addIconRow(StatIcons.buff(StatIcons.BUFF_LIGHT), "Vision", ExtraStats.vision(hero) + " tiles");
		addIconRow(StatIcons.buff(StatIcons.BUFF_MIND_VISION), "Search range", ExtraStats.searchRange(hero) + " tiles");
		int searchChance = HeroExtras.mod(HeroExtras.Mod.SEARCH_CHANCE);
		addIconRow(StatIcons.buff(StatIcons.BUFF_FORESIGHT), "Search chance", searchChance == 0 ? "Normal" : (searchChance > 0 ? "+" : "") + searchChance + "%");
		addIconRow(StatIcons.buff(StatIcons.BUFF_INVISIBLE), "Stealth", StatsWindow.number(ExtraStats.stealth(hero)));
		addIconRow(StatIcons.item(StatIcons.RING_WEALTH), "Luck", signed(ExtraStats.ringLevel(hero, HeroExtras.Mod.LUCK)));
		addIconRow(StatIcons.item(StatIcons.RING_ARCANA), "Enchant power", signed(ExtraStats.ringLevel(hero, HeroExtras.Mod.ENCHANT_POWER)));

		boolean rings = false;
		for (HeroExtras.Mod mod : HeroExtras.Mod.in(HeroExtras.Group.RINGS)) {
			int level = ExtraStats.ringLevel(hero, mod);
			if (level == 0) continue;
			if (!rings) addSection("Ring levels");
			rings = true;
			addIconRow(CharacterWindow.modIcon(mod), mod.label, signed(level));
		}

		boolean others = false;
		for (HeroExtras.Mod mod : OTHER_MODS) {
			int value = HeroExtras.mod(mod);
			if (value == 0) continue;
			if (!others) addSection("Other modifiers");
			others = true;
			addIconRow(CharacterWindow.modIcon(mod), mod.label, (value > 0 ? "+" : "") + value + "%");
		}

		ExtraStats.addResistances(this, hero);
		pos += GAP;
		addButton("Close", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private static String signed(int value) {
		return (value > 0 ? "+" : "") + value;
	}

	@Override
	protected void reopen() {
		show(new HeroStatsWindow());
	}
}
