package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * Tile maths on the current floor.
 */
final class Grid {

	private Grid() {
	}

	/**
	 * Steps between two cells, counting diagonal steps as one.
	 */
	static int distance(int a, int b, int width) {
		return Math.max(Math.abs(a % width - b % width), Math.abs(a / width - b / width));
	}

	static int distance(int a, int b) {
		return distance(a, b, Dungeon.level.width());
	}

	/**
	 * The tiles of [level] that can be walked on and that the hero has seen or mapped, the way the game picks routes for taps.
	 */
	static boolean[] knownPassable(Level level) {
		int length = level.length();
		boolean[] passable = new boolean[length];
		for (int i = 0; i < length; i++) passable[i] = level.passable[i] && (level.visited[i] || level.mapped[i]);
		return passable;
	}
}
