package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/**
 * A Remove button in the header of a status's info window, for your hero with the Character editor and for
 * creatures with the Enemy editor.
 */
@SuppressWarnings("unused")
public final class BuffInfoActions {

	private static final String TAG = "BuffInfoActions";
	private static final int REMOVE_WIDTH = 42;

	private static Runnable onRemoved;

	private BuffInfoActions() {
	}

	static void setOnRemoved(Runnable action) {
		onRemoved = action;
	}

	/**
	 * Adds a Remove button to a status's info window.
	 */
	static void decorateInfo(Window window, Object pending) {
		if (!(pending instanceof Buff)) return;
		Runnable after = onRemoved;
		onRemoved = null;
		Buff buff = (Buff) pending;
		boolean hero = buff.target != null && buff.target == Dungeon.hero;
		boolean creature = buff.target instanceof Mob;
		if (hero && !EditorSwitches.character()) return;
		if (creature && !(EnemyWindow.isAvailable() && EditorSwitches.enemy())) return;
		if (!hero && !creature) return;
		Window info = window;
		try {
			WindowHeader.add(info, WindowHeader.smallButton("Remove", Icons.CLOSE, () -> {
				info.hide();
				if (hero) {
					InfiniteStatuses.removeForever(buff);
				} else {
					StatusEditor.remove(buff);
				}
				try {
					BuffIndicator.refreshHero();
				} catch (Throwable ignored) {
				}
				if (after != null) after.run();
			}), REMOVE_WIDTH, WindowHeader.SMALL_HEIGHT, WindowHeader.PRIORITY_ACTION);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the Remove button", e);
		}
	}
}
