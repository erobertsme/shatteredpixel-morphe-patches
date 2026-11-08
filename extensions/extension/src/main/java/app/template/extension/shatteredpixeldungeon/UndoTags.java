package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * The Undo / Redo toolbar tag. Tapping it opens undo mode: Undo on the left, Done in the middle and Redo on the
 * right, above the toolbar. Undo mode stays open across the reload, so you can step back several times.
 */
@SuppressWarnings("unused")
public final class UndoTags {

	static final TagPlacement PLACEMENT = new TagPlacement("qol_undo_tags", TagPlacement.Side.LEFT, 0.35f, 1, "CHANGES", 0).defaultOffset(52).defaultOpacity(70);

	/**
	 * Below the hero portrait and the busy circle under it.
	 */
	static final int UNDO_TOP = 52;

	private static final int BUTTON_WIDTH = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int MARGIN = 4;
	private static final int TEXT_GAP = 6;
	private static final int DEATH_BUTTON_WIDTH = 80;
	private static final float DEATH_FADE = 2f;

	private static boolean modeOpen;
	private static final List<Gizmo> overlay = new ArrayList<>();

	private UndoTags() {
	}

	/**
	 * Called at the end of GameScene.create.
	 */
	public static void onSceneCreated(Object scene) {
		if (!(scene instanceof Scene)) return;
		overlay.clear();
		if (!UndoHistory.isAvailable()) return;
		try {
			Scene target = (Scene) scene;
			IconTag tag = new IconTag(() -> openMode(target));
			tag.camera = PixelScene.uiCamera;
			Gizmo controller = new Gizmo() {
				@Override
				public void update() {
					super.update();
					tag.visible = UndoHistory.isOn() && !modeOpen && Dungeon.hero != null && Dungeon.hero.isAlive();
					if (tag.visible) PLACEMENT.place(tag);
				}
			};
			target.add(tag);
			target.add(controller);
			if (modeOpen && UndoHistory.isOn()) buildMode(target);
		} catch (Throwable e) {
			Log.e("UndoTags", "Could not add the Undo / Redo tag", e);
		}
	}

	private static void openMode(Scene scene) {
		if (modeOpen) return;
		modeOpen = true;
		buildMode(scene);
	}

	private static void closeMode() {
		modeOpen = false;
		for (Gizmo gizmo : overlay) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		overlay.clear();
	}

	private static void buildMode(Scene scene) {
		Camera ui = PixelScene.uiCamera;
		float top = ScreenLayout.aboveToolbar() - BUTTON_HEIGHT;

		RenderedTextBlock label = PixelScene.renderTextBlock("", 9);
		label.hardlight(0xFFFF44);
		addToOverlay(scene, label);

		TintedButton undo = new TintedButton("Undo", null, 1f, 1f, 1f, UndoHistory::undo);
		undo.icon(StatIcons.arrow(true));
		undo.setRect(MARGIN, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, undo);

		TintedButton done = new TintedButton("Done", null, 1f, 1f, 1f, UndoTags::closeMode);
		done.setRect((ui.width - BUTTON_WIDTH) / 2f, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, done);

		TintedButton redo = new TintedButton("Redo", null, 1f, 1f, 1f, UndoHistory::redo);
		redo.icon(StatIcons.arrow(false));
		redo.setRect(ui.width - MARGIN - BUTTON_WIDTH, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, redo);

		Gizmo watcher = new Gizmo() {
			private String shown;

			@Override
			public void update() {
				super.update();
				if (!UndoHistory.isOn() || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
					closeMode();
					return;
				}
				undo.enable(UndoHistory.canUndo());
				redo.enable(UndoHistory.canRedo());
				String text = UndoHistory.positionText();
				if (text.equals(shown)) return;
				shown = text;
				label.text(text);
				label.setPos((ui.width - label.width()) / 2f, top - TEXT_GAP - label.height());
				PixelScene.align(label);
			}
		};
		addToOverlay(scene, watcher);
	}

	/**
	 * Adds an Undo button below the game over screen's New Game and Menu buttons. It fades in with them.
	 */
	static void addDeathButton() {
		Scene scene = com.watabou.noosa.Game.scene();
		if (scene == null) return;
		Camera ui = PixelScene.uiCamera;
		com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton undo = new com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton(com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON_TR, "Undo") {
			private float shown;

			@Override
			public void onClick() {
				UndoHistory.undoDeath();
			}

			@Override
			public void update() {
				shown = Math.min(DEATH_FADE, shown + com.watabou.noosa.Game.elapsed);
				alpha((float) Math.pow(shown / DEATH_FADE, 2));
				super.update();
			}
		};
		undo.icon(StatIcons.arrow(true));
		undo.alpha(0);
		undo.camera = ui;
		undo.setRect((ui.width - DEATH_BUTTON_WIDTH) / 2f, (ui.height - BUTTON_HEIGHT) / 2f + 8 + 2 * (BUTTON_HEIGHT + 2), DEATH_BUTTON_WIDTH, BUTTON_HEIGHT);
		PixelScene.align(undo);
		scene.add(undo);
	}

	private static void addToOverlay(Scene scene, Gizmo gizmo) {
		gizmo.camera = PixelScene.uiCamera;
		scene.add(gizmo);
		overlay.add(gizmo);
	}
}
