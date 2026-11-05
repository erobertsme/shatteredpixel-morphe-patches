package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

public class Dungeon {
	public static Hero hero;
	public static Level level;
	public static int gold;
	public static int depth;
	public static long seed;

	public static void saveAll() throws java.io.IOException { throw new RuntimeException("Stub"); }

	public static com.watabou.utils.PathFinder.Path findPath(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch, int to, boolean[] pass, boolean[] vis, boolean chars) { throw new RuntimeException("Stub"); }
	public static boolean bossLevel() { throw new RuntimeException("Stub"); }
	public static int scalingDepth() { throw new RuntimeException("Stub"); }
	public static void observe() { throw new RuntimeException("Stub"); }
	public static int energy;
}
