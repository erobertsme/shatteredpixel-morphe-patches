package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the rest of an interrupted walk while the game's resume button is on screen.
 */
@SuppressWarnings("unused")
public final class ResumePath {

	private static final String TAG = "ResumePath";
	private static final int TILE_SIZE = 16;
	private static final int STEP_SIZE = 4;
	private static final int TARGET_SIZE = 10;
	private static final int PATH_COLOR = 0xFFFFFF44;
	private static final int TARGET_COLOR = 0xFF7AD35C;

	private static final List<Gizmo> markers = new ArrayList<>();
	private static Scene shownScene;
	private static HeroAction shownAction;
	private static int shownHeroPos = -1;

	private ResumePath() {
	}

	/**
	 * Called at the end of ResumeIndicator.update.
	 */
	public static void afterIndicatorUpdate(Object indicator) {
		try {
			Hero hero = Dungeon.hero;
			Scene scene = Game.scene();
			HeroAction action = !QolSettings.resumePath() || hero == null || !hero.isAlive() || !hero.ready || !(scene instanceof GameScene) ? null : hero.lastAction;
			if (action == null) {
				clear();
				return;
			}
			if (action == shownAction && scene == shownScene && hero.pos == shownHeroPos) return;
			clear();
			show(scene, hero, action);
		} catch (Throwable e) {
			Log.e(TAG, "Could not show the interrupted path", e);
			clear();
		}
	}

	private static void show(Scene scene, Hero hero, HeroAction action) {
		shownScene = scene;
		shownAction = action;
		shownHeroPos = hero.pos;
		Level level = Dungeon.level;
		int target = action.dst;
		if (level == null || target < 0 || target >= level.length() || target == hero.pos) return;
		int width = level.width();
		for (int cell : findPath(hero, target, level)) {
			if (cell != target) addMarker(scene, cell, width, STEP_SIZE, PATH_COLOR);
		}
		addMarker(scene, target, width, TARGET_SIZE, TARGET_COLOR);
	}

	private static List<Integer> findPath(Hero hero, int cell, Level level) {
		int length = level.length();
		boolean[] passable = new boolean[length];
		for (int i = 0; i < length; i++) passable[i] = level.passable[i] && (level.visited[i] || level.mapped[i]);
		PathFinder.Path path = Dungeon.findPath(hero, cell, passable, hero.fieldOfView, true);
		return path == null ? new ArrayList<>() : new ArrayList<>(path);
	}

	private static void addMarker(Scene scene, int cell, int width, int size, int color) {
		ColorBlock block = new ColorBlock(size, size, color);
		block.x = (cell % width) * TILE_SIZE + (TILE_SIZE - size) / 2f;
		block.y = (cell / width) * TILE_SIZE + (TILE_SIZE - size) / 2f;
		block.alpha(0.8f);
		scene.add(block);
		markers.add(block);
	}

	private static void clear() {
		shownScene = null;
		shownAction = null;
		shownHeroPos = -1;
		if (markers.isEmpty()) return;
		for (Gizmo gizmo : markers) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		markers.clear();
	}
}
