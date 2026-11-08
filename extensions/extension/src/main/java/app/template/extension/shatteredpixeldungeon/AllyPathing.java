package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.PathFinder;

import java.lang.reflect.Field;

/**
 * Path through allies: walking to a far tile swaps places with allies in the way when there is no other route.
 */
@SuppressWarnings("unused")
public final class AllyPathing {

	private static final String KEY_OFF = "qol_ally_pathing_off";
	private static final int CONTINUE = -1;
	private static final int STOPPED = 0;
	private static final int MOVED = 1;

	private static Field pathField;

	private AllyPathing() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && !CloverlordSettings.get(KEY_OFF);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_OFF, !on);
	}

	/**
	 * Called at the start of Hero.getCloser. Returns -1 to run the game's own step, otherwise 1 if the hero swapped with an ally and 0 if it stopped.
	 */
	public static int beforeGetCloser(Object hero, int target) {
		if (!(hero instanceof Char) || !isOn()) return CONTINUE;
		Char walker = (Char) hero;
		Level level = Dungeon.level;
		if (level == null || target == walker.pos || walker.rooted || level.adjacent(walker.pos, target)) return CONTINUE;
		try {
			PathFinder.Path path = path(walker);
			if (needsNewPath(walker, level, path, target)) {
				boolean[] known = knownPassable(level);
				if (Dungeon.findPath(walker, target, known, walker.fieldOfView, true) != null) return CONTINUE;
				path = allyPath(walker, target, known);
				if (path == null) return CONTINUE;
				pathField.set(walker, path);
			}
			Char ally = Actor.findChar(path.getFirst());
			if (!swappable(ally)) return CONTINUE;
			int from = walker.pos;
			ally.interact(walker);
			if (walker.pos == from) {
				pathField.set(walker, null);
				return STOPPED;
			}
			path.removeFirst();
			return MOVED;
		} catch (Throwable e) {
			Log.e("AllyPathing", "Could not path through allies", e);
			return CONTINUE;
		}
	}

	private static PathFinder.Path path(Char walker) throws ReflectiveOperationException {
		if (pathField == null) {
			pathField = Char.class.getDeclaredField("path");
			pathField.setAccessible(true);
		}
		return (PathFinder.Path) pathField.get(walker);
	}

	private static boolean needsNewPath(Char walker, Level level, PathFinder.Path path, int target) {
		if (path == null || path.isEmpty() || path.getLast() != target) return true;
		int first = path.getFirst();
		if (!level.adjacent(walker.pos, first) || !level.passable[first]) return true;
		Char blocker = Actor.findChar(first);
		return blocker != null && !swappable(blocker);
	}

	private static boolean[] knownPassable(Level level) {
		int length = level.length();
		boolean[] passable = new boolean[length];
		for (int i = 0; i < length; i++) passable[i] = level.passable[i] && (level.visited[i] || level.mapped[i]);
		return passable;
	}

	private static PathFinder.Path allyPath(Char walker, int target, boolean[] known) {
		boolean[] passable = Dungeon.findPassable(walker, known, walker.fieldOfView, true);
		boolean any = false;
		for (Char c : Actor.chars()) {
			if (!swappable(c) || !known[c.pos]) continue;
			passable[c.pos] = true;
			any = true;
		}
		if (!any) return null;
		return PathFinder.find(walker.pos, target, passable);
	}

	private static boolean swappable(Char c) {
		if (!(c instanceof Mob) || c.alignment != Char.Alignment.ALLY || !c.isAlive()) return false;
		if (c.properties().contains(Char.Property.IMMOVABLE)) return false;
		return !c.rooted && c.paralysed <= 0;
	}
}
