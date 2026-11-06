package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

/**
 * The hero stats popup, opened from the stats button on the hero info window.
 */
@SuppressWarnings("unused")
public class HeroStatsWindow extends EditorWindow {

	private static final int BUTTON_SIZE = 16;

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
			Field membersField = Group.class.getDeclaredField("members");
			membersField.setAccessible(true);
			float left = -1;
			float bottom = 0;
			for (Gizmo member : (List<Gizmo>) membersField.get(tab)) {
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
		pos += GAP;
		addButton("Close", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new HeroStatsWindow());
	}
}
