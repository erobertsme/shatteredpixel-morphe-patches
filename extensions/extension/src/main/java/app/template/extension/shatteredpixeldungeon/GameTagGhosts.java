package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Faded outlines of the game's own situational buttons (Attack, Pick Up, Action and Resume) where they appear
 * when needed, shown while a tag is being positioned so it can be kept clear of them.
 */
final class GameTagGhosts {

	private static final float ALPHA = 0.45f;

	private static final List<Gizmo> ghosts = new ArrayList<>();

	private GameTagGhosts() {
	}

	private static final class Ghost extends Tag {
		Ghost(int color) {
			super(color);
		}

		void fade() {
			if (bg != null) bg.alpha(ALPHA);
		}

		@Override
		public void onClick() {
		}
	}

	static void show() {
		hide();
		Scene scene = Game.scene();
		if (scene == null) return;
		float[][] rects = GameTags.rects();
		for (int i = 0; i < rects.length; i++) {
			for (Gizmo part : ghost(i, rects[i])) add(scene, part);
		}
	}

	/**
	 * A faded outline of game tag [index] at [rect], with its name on it, as the tag and its label.
	 */
	static Gizmo[] ghost(int index, float[] rect) {
		Camera ui = PixelScene.uiCamera;
		Ghost ghost = new Ghost(GameTags.PLACEMENTS[index].color());
		ghost.camera = ui;
		ghost.setRect(rect[0], rect[1], rect[2], rect[3]);
		ghost.flip(GameTags.flipped(index));
		ghost.fade();
		RenderedTextBlock label = PixelScene.renderTextBlock(GameTags.NAMES[index], 5);
		label.alpha(0.85f);
		label.camera = ui;
		label.setPos(rect[0] + (rect[2] - label.width()) / 2f, rect[1] + (rect[3] - label.height()) / 2f);
		PixelScene.align(label);
		return new Gizmo[] { ghost, label };
	}

	private static void add(Scene scene, Gizmo gizmo) {
		scene.add(gizmo);
		ghosts.add(gizmo);
	}

	static void hide() {
		for (Gizmo gizmo : ghosts) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		ghosts.clear();
	}
}
