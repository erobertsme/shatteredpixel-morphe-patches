package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
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

	private static final String[] LABELS = { "Attack", "Pick Up", "Action", "Resume" };
	private static final int[] COLORS = { 0xC03838, 0x185898, 0xC0A040, 0xA3A695 };
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
		Camera ui = PixelScene.uiCamera;
		boolean onLeft = SPDSettings.flipTags();
		float left = onLeft ? 0 : ui.width - Tag.SIZE;
		float pos = ScreenLayout.aboveToolbar() + 4;
		float[] attackTile = ForceAttack.PLACEMENT.isAuto() ? ForceAttack.autoRect() : null;
		if (attackTile != null) pos -= attackTile[3];
		else if (!(scene instanceof GameScene) && ForceAttack.PLACEMENT.isAuto()) pos -= ForceAttack.PLACEMENT.tagHeight();
		for (int i = 0; i < LABELS.length; i++) {
			pos -= Tag.SIZE;
			Ghost ghost = new Ghost(COLORS[i]);
			ghost.camera = ui;
			ghost.setRect(left, pos, Tag.SIZE, Tag.SIZE);
			ghost.flip(onLeft);
			ghost.fade();
			add(scene, ghost);

			RenderedTextBlock label = PixelScene.renderTextBlock(LABELS[i], 5);
			label.alpha(0.85f);
			label.camera = ui;
			label.setPos(left + (Tag.SIZE - label.width()) / 2f, pos + (Tag.SIZE - label.height()) / 2f);
			PixelScene.align(label);
			add(scene, label);
		}
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
