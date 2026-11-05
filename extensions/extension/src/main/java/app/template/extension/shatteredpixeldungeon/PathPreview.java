package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Asks before a tap moves, attacks, talks, uses stairs or steps into danger, showing what will happen with Go and Cancel buttons.
 */
@SuppressWarnings("unused")
public final class PathPreview {

	private static final String TAG = "PathPreview";
	private static final int ATTACK_COLOR = 0xFFE06A5A;
	private static final int DANGER_COLOR = 0xFFFF8844;
	private static final int ENTRANCE = 7;
	private static final int EXIT = 8;
	private static final int UNLOCKED_EXIT = 22;
	private static final int ENTRANCE_SP = 37;
	private static final String[] HARMFUL_EFFECTS = {
		"Fire", "ToxicGas", "CorrosiveGas", "ParalyticGas", "ConfusionGas", "Electricity", "Inferno", "Blizzard", "Freezing", "StenchGas",
	};
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
		return isAvailable() && ConfirmSettings.isOn();
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
			if (!level.visited[cell] && !level.mapped[cell] && !hero.fieldOfView[cell]) return false;

			boolean adjacent = isAdjacent(hero.pos, cell, level.width());
			String danger = dangerAt(level, cell);
			Char ch = Actor.findChar(cell);
			String message;
			String goLabel;
			int color;
			boolean moving = false;
			int distance = distance(hero.pos, cell, level.width());
			if (ch instanceof Mob && ch != hero && hero.fieldOfView[cell] && !FloorEditor.isHiddenMimic(ch)) {
				boolean interact = ((Mob) ch).heroShouldInteract();
				if (!ConfirmSettings.confirms(interact ? ConfirmSettings.Action.INTERACT : ConfirmSettings.Action.ATTACK, distance)) return false;
				message = (interact ? "Talk to " : "Attack ") + ch.name() + "?";
				goLabel = interact ? "Talk" : "Attack";
				color = interact ? TARGET_COLOR : ATTACK_COLOR;
			} else if (danger != null && ConfirmSettings.confirms(ConfirmSettings.Action.DANGER)) {
				message = "Danger: " + danger;
				goLabel = "Go";
				color = DANGER_COLOR;
			} else if (isStairs(level.map[cell])) {
				if (!ConfirmSettings.confirms(ConfirmSettings.Action.STAIRS)) return false;
				message = "Use the " + (level.map[cell] == ENTRANCE || level.map[cell] == ENTRANCE_SP ? "stairs up" : "stairs down") + "?";
				goLabel = "Go";
				color = TARGET_COLOR;
			} else if (adjacent) {
				if (!ConfirmSettings.confirms(ConfirmSettings.Action.MOVE, 1)) return false;
				message = "Step here?";
				goLabel = "Go";
				color = TARGET_COLOR;
			} else {
				if (!ConfirmSettings.confirms(ConfirmSettings.Action.MOVE)) return false;
				moving = true;
				message = null;
				goLabel = "Go";
				color = TARGET_COLOR;
			}

			List<Integer> path = adjacent ? new ArrayList<>() : findPath(hero, cell, level);
			if (path == null) path = new ArrayList<>();
			if (message == null) {
				if (path.isEmpty()) return false;
				if (moving && path.size() < ConfirmSettings.minDistance(ConfirmSettings.Action.MOVE)) return false;
				message = "Walk " + path.size() + (path.size() == 1 ? " step?" : " steps?");
			}

			active = new PathPreview(cell, hero.pos);
			active.show(path, message, goLabel, color);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not preview the path", e);
			clear();
			return false;
		}
	}

	private static boolean isStairs(int tile) {
		return tile == ENTRANCE || tile == EXIT || tile == UNLOCKED_EXIT || tile == ENTRANCE_SP;
	}

	private static String dangerAt(Level level, int cell) {
		if (level.map[cell] == 0) return "a chasm. You will fall to the floor below.";
		com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap = level.traps == null ? null : level.traps.get(cell, null);
		if (trap != null && trap.visible && trap.active) return "a " + FloorEditor.trapName(trap).toLowerCase() + ".";
		for (com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob blob : FloorEditor.activeEffects()) {
			if (FloorEditor.strengthAt(blob, cell) <= 0) continue;
			for (String harmful : HARMFUL_EFFECTS) {
				if (blob.getClass().getSimpleName().equals(harmful)) return FloorEditor.effectName(blob.getClass()).toLowerCase() + ".";
			}
		}
		return null;
	}

	private static boolean isAdjacent(int a, int b, int width) {
		return distance(a, b, width) <= 1;
	}

	private static int distance(int a, int b, int width) {
		return Math.max(Math.abs(a % width - b % width), Math.abs(a / width - b / width));
	}

	private static List<Integer> findPath(Hero hero, int cell, Level level) {
		int length = level.length();
		boolean[] passable = new boolean[length];
		for (int i = 0; i < length; i++) passable[i] = level.passable[i] && (level.visited[i] || level.mapped[i]);
		PathFinder.Path path = Dungeon.findPath(hero, cell, passable, hero.fieldOfView, true);
		return path == null ? null : new ArrayList<>(path);
	}

	private void show(List<Integer> path, String message, String goLabel, int color) {
		int width = Dungeon.level.width();
		for (int cell : path) {
			if (cell == target) continue;
			addMarker(cell, width, STEP_SIZE, PATH_COLOR);
		}
		addMarker(target, width, TARGET_SIZE, color);

		RenderedTextBlock label = PixelScene.renderTextBlock(message, 9);
		label.hardlight(color == ATTACK_COLOR || color == DANGER_COLOR ? 0xFF8866 : 0xFFFF44);
		label.maxWidth(PixelScene.uiCamera.width - 2 * MARGIN);
		label.camera = PixelScene.uiCamera;
		label.setPos(MARGIN, 30);
		scene.add(label);
		overlay.add(label);

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

		TintedButton go = TintedButton.finish(goLabel, PathPreview::go);
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
