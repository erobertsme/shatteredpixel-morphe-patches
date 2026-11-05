package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the route the hero would walk to a tapped tile, with Go and Cancel buttons, before moving.
 */
@SuppressWarnings("unused")
public final class PathPreview {

	private static final String TAG = "PathPreview";
	private static final String KEY_OFF = "qol_confirm_movement_off";
	private static final int TILE_SIZE = 16;
	private static final int STEP_SIZE = 4;
	private static final int TARGET_SIZE = 10;
	private static final int PATH_COLOR = 0xFFFFFF44;
	private static final int TARGET_COLOR = 0xFF7AD35C;
	private static final int BUTTON_WIDTH = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int MARGIN = 4;

	private static PathPreview active;

	private final Scene scene;
	private final int target;
	private final int heroPos;
	private final List<Gizmo> overlay = new ArrayList<>();

	private PathPreview(int target, int heroPos) {
		this.scene = Game.scene();
		this.target = target;
		this.heroPos = heroPos;
	}

	/**
	 * Replaced by the QoL enhancements patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	static boolean isEnabled() {
		return isAvailable() && !CloverlordSettings.get(KEY_OFF);
	}

	static void setEnabled(boolean enabled) {
		CloverlordSettings.set(KEY_OFF, !enabled);
		if (!enabled) clear();
	}

	/**
	 * Called at the start of the game's default tap handler. Returns true when the tap was used for a preview.
	 */
	public static boolean interceptTap(Integer cell) {
		try {
			if (active != null && cell != null && cell == active.target && active.isCurrent()) {
				go();
				return true;
			}
			clear();
			if (cell == null || !isEnabled()) return false;

			Hero hero = Dungeon.hero;
			Level level = Dungeon.level;
			if (hero == null || level == null || !hero.isAlive() || !hero.ready) return false;
			if (cell < 0 || cell >= level.length() || cell == hero.pos) return false;
			if (!level.visited[cell] && !level.mapped[cell]) return false;
			if (isAdjacent(hero.pos, cell, level.width())) return false;
			if (Actor.findChar(cell) != null) return false;

			List<Integer> path = findPath(hero, cell, level);
			if (path == null || path.isEmpty()) return false;

			active = new PathPreview(cell, hero.pos);
			active.show(path);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not preview the path", e);
			clear();
			return false;
		}
	}

	private static boolean isAdjacent(int a, int b, int width) {
		return Math.abs(a % width - b % width) <= 1 && Math.abs(a / width - b / width) <= 1;
	}

	private static List<Integer> findPath(Hero hero, int cell, Level level) {
		int length = level.length();
		boolean[] passable = new boolean[length];
		for (int i = 0; i < length; i++) passable[i] = level.passable[i] && (level.visited[i] || level.mapped[i]);
		PathFinder.Path path = Dungeon.findPath(hero, cell, passable, hero.fieldOfView, true);
		return path == null ? null : new ArrayList<>(path);
	}

	private void show(List<Integer> path) {
		int width = Dungeon.level.width();
		for (int cell : path) {
			if (cell == target) continue;
			addMarker(cell, width, STEP_SIZE, PATH_COLOR);
		}
		addMarker(target, width, TARGET_SIZE, TARGET_COLOR);

		Gizmo watcher = new Gizmo() {
			@Override
			public void update() {
				super.update();
				if (!isCurrent()) clear();
			}
		};
		scene.add(watcher);
		overlay.add(watcher);

		float buttonsTop = PixelScene.uiCamera.height - BUTTON_HEIGHT - 30;
		TintedButton cancel = TintedButton.cancel("Cancel", PathPreview::clear);
		cancel.camera = PixelScene.uiCamera;
		cancel.setRect(MARGIN, buttonsTop, BUTTON_WIDTH, BUTTON_HEIGHT);
		scene.add(cancel);
		overlay.add(cancel);

		TintedButton go = TintedButton.finish("Go", PathPreview::go);
		go.camera = PixelScene.uiCamera;
		go.setRect(PixelScene.uiCamera.width - MARGIN - BUTTON_WIDTH, buttonsTop, BUTTON_WIDTH, BUTTON_HEIGHT);
		scene.add(go);
		overlay.add(go);
	}

	private void addMarker(int cell, int width, int size, int color) {
		ColorBlock block = new ColorBlock(size, size, color);
		block.x = (cell % width) * TILE_SIZE + (TILE_SIZE - size) / 2f;
		block.y = (cell / width) * TILE_SIZE + (TILE_SIZE - size) / 2f;
		block.alpha(0.8f);
		scene.add(block);
		overlay.add(block);
	}

	private boolean isCurrent() {
		return scene == Game.scene() && Dungeon.hero != null && Dungeon.hero.pos == heroPos && Dungeon.hero.isAlive();
	}

	private static void go() {
		PathPreview preview = active;
		clear();
		if (preview == null || !preview.isCurrent()) return;
		Hero hero = Dungeon.hero;
		if (hero.handle(preview.target)) hero.next();
	}

	static void clear() {
		PathPreview preview = active;
		active = null;
		if (preview == null) return;
		for (Gizmo gizmo : preview.overlay) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		preview.overlay.clear();
	}
}
