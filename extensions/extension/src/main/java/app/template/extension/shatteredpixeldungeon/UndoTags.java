package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;

/**
 * The Undo and Redo toolbar tags.
 */
@SuppressWarnings("unused")
public final class UndoTags {

	static final TagPlacement PLACEMENT = new TagPlacement("qol_undo_tags", TagPlacement.Side.LEFT, 0.35f, 2);

	private UndoTags() {
	}

	/**
	 * Called at the end of GameScene.create.
	 */
	public static void onSceneCreated(Object scene) {
		if (!QolSettings.isAvailable() || !(scene instanceof Scene)) return;
		try {
			IconTag undo = new IconTag(StatIcons.arrow(true), UndoHistory::undo);
			IconTag redo = new IconTag(StatIcons.arrow(false), UndoHistory::redo);
			Gizmo controller = new Gizmo() {
				@Override
				public void update() {
					super.update();
					boolean show = UndoHistory.isOn() && Dungeon.hero != null && Dungeon.hero.isAlive();
					undo.visible = redo.visible = show;
					if (!show) return;
					undo.usable(UndoHistory.canUndo());
					redo.usable(UndoHistory.canRedo());
					PLACEMENT.place(undo, redo);
				}
			};
			undo.camera = PixelScene.uiCamera;
			redo.camera = PixelScene.uiCamera;
			Scene target = (Scene) scene;
			target.add(undo);
			target.add(redo);
			target.add(controller);
		} catch (Throwable e) {
			Log.e("UndoTags", "Could not add the Undo and Redo tags", e);
		}
	}
}
