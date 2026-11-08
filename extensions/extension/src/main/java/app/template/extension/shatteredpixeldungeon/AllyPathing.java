package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.PathFinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Path through allies: walking to a far tile swaps places with allies in the way, and walks over wards, when there is no other route.
 */
@SuppressWarnings("unused")
public final class AllyPathing {

	private static final String KEY_OFF = "qol_ally_pathing_off";
	private static final int CONTINUE = -1;
	private static final int STOPPED = 0;
	private static final int MOVED = 1;
	private static final String WARD_CLASS = "com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding$Ward";
	private static final String GHOST_CLASS = "com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose$GhostHero";

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
			if (isWard(ally)) {
				stepOnto(walker, path.removeFirst());
				return MOVED;
			}
			int from = walker.pos;
			swap(walker, ally);
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

	/**
	 * The route the hero takes to [target]: the game's own, or one through allies and wards when that is the only way.
	 */
	static PathFinder.Path route(Char walker, int target) {
		Level level = Dungeon.level;
		boolean[] known = knownPassable(level);
		PathFinder.Path path = Dungeon.findPath(walker, target, known, walker.fieldOfView, true);
		if (path != null || !isOn()) return path;
		return allyPath(walker, target, known);
	}

	private static void swap(Char walker, Char ally) throws ReflectiveOperationException {
		if (!ally.getClass().getName().equals(GHOST_CLASS)) {
			ally.interact(walker);
			return;
		}
		Method update = ally.getClass().getDeclaredMethod("updateRose");
		update.setAccessible(true);
		update.invoke(ally);
		Field roseField = ally.getClass().getDeclaredField("rose");
		roseField.setAccessible(true);
		Object rose = roseField.get(ally);
		if (rose == null) {
			ally.interact(walker);
			return;
		}
		Field talked = rose.getClass().getDeclaredField("talkedTo");
		talked.setAccessible(true);
		boolean wasTalked = talked.getBoolean(rose);
		talked.setBoolean(rose, true);
		try {
			ally.interact(walker);
		} finally {
			talked.setBoolean(rose, wasTalked);
		}
	}

	private static void stepOnto(Char walker, int to) throws ReflectiveOperationException {
		Method moveSprite = Char.class.getDeclaredMethod("moveSprite", int.class, int.class);
		moveSprite.setAccessible(true);
		Method spend = Actor.class.getDeclaredMethod("spend", float.class);
		spend.setAccessible(true);
		moveSprite.invoke(walker, walker.pos, to);
		walker.move(to);
		spend.invoke(walker, 1f / walker.speed());
		walker.getClass().getMethod("search", boolean.class).invoke(walker, false);
	}

	private static boolean isWard(Char c) {
		for (Class<?> type = c.getClass(); type != null; type = type.getSuperclass()) {
			if (type.getName().equals(WARD_CLASS)) return true;
		}
		return false;
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
		if (isWard(c)) return true;
		if (c.properties().contains(Char.Property.IMMOVABLE)) return false;
		return !c.rooted && c.paralysed <= 0;
	}
}
