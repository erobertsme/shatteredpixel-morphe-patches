package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;

import java.util.ArrayList;
import java.util.List;

/**
 * A game save shown the way undo mode shows a step: the hero at the time with their talents and backpack, the dungeon around them, the last
 * game messages, and buttons to load, delete or go back. In landscape the picture sits beside the rest.
 */
public class SavePreviewWindow extends EditorWindow {

	private static final int STATS_WIDTH = 40;
	private static final int BACKPACK_WIDTH = 60;
	private static final int BACKPACK_HEIGHT = 16;
	static final int TOP_BUTTON_WIDTH = 50;
	static final int TOP_BUTTON_HEIGHT = 20;

	private final GameSaves.Save save;
	private final int page;
	private final boolean fromGameMenu;

	public SavePreviewWindow(GameSaves.Save save, int page, boolean fromGameMenu) {
		super();
		this.save = save;
		this.page = page;
		this.fromGameMenu = fromGameMenu;
		addTitle(save.title());
		float columnTop = pos;

		UndoScene scene = save.preview();
		Hero hero = save.hero();
		boolean hasPicture = scene != null && (scene.hasArea() || scene.hasMap());
		boolean wide = PixelScene.landscape() && hasPicture;
		addHero(hero, scene);
		if (!hasPicture) {
			addHint("No picture for this save. Saves made from now on keep one.");
		} else {
			addLog(scene.lines);
		}
		float pictureBottom = pos;
		if (hasPicture && !wide) {
			addPicture(scene, 0, pos);
			pictureBottom = pos;
		}
		if (wide) pictureBottom = addPicture(scene, WIDTH + 2 * GAP, columnTop);

		float half = (WIDTH - GAP) / 2f;
		addButton("Load", 0, half, this::load);
		addButton(save.locked ? "Unlock" : "Lock", half + GAP, half, this::toggleLock);
		pos += ROW_HEIGHT + GAP;
		addButton("Delete", 0, half, this::delete).enable(!save.locked);
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT;
		resize(wide ? 2 * WIDTH + 2 * GAP : WIDTH, (int) Math.max(pos, pictureBottom));
	}

	private void addHero(Hero hero, UndoScene scene) {
		float top = pos;
		String[] stats = {
			"Depth " + (scene != null && scene.depth >= 0 ? scene.depth : "-"),
			"Turn " + (scene != null && scene.turn >= 0 ? scene.turn : "-"),
		};
		List<Gizmo> parts = new ArrayList<>();
		PreviewParts.statLines(parts, stats, WIDTH, top, STATS_WIDTH);
		if (hero == null) {
			addAll(parts);
			pos = top + stats.length * PreviewParts.STAT_LINE_HEIGHT + GAP;
			return;
		}
		try {
			UndoHeroView.build(parts, hero, 0, top, WIDTH - STATS_WIDTH - 4);
		} catch (Throwable e) {
			Log.e("SavePreviewWindow", "Could not show the hero", e);
		}
		addAll(parts);
		pos = top + UndoHeroView.HEIGHT + 3;
		RedButton backpack = new RedButton("Backpack", 7) {
			@Override
			public void onClick() {
				hide();
				show(new SaveBackpackWindow(hero, save, page, fromGameMenu));
			}
		};
		backpack.icon(Icons.get(Icons.BACKPACK));
		backpack.setRect(0, pos, BACKPACK_WIDTH, BACKPACK_HEIGHT);
		add(backpack);
		pos += BACKPACK_HEIGHT + 2 * GAP;
	}

	/**
	 * Adds the picture with its top left corner at [x], [y]. Returns the bottom of the picture. Below the rest, it also moves pos past it.
	 */
	private float addPicture(UndoScene scene, float x, float y) {
		float size = WIDTH - 2 * PreviewParts.BORDER;
		List<Gizmo> parts = new ArrayList<>();
		PreviewParts.frame(parts, x + PreviewParts.BORDER, y + PreviewParts.BORDER, size);
		PreviewParts.picture(parts, scene, null, x + PreviewParts.BORDER, y + PreviewParts.BORDER, size, this::refresh);
		addAll(parts);
		float bottom = y + size + 2 * PreviewParts.BORDER + 2 * GAP;
		if (x == 0) pos = bottom;
		return bottom;
	}

	private void addLog(List<String> lines) {
		List<Gizmo> parts = new ArrayList<>();
		PreviewParts.logBackground(parts, 0, pos, WIDTH);
		PreviewParts.logLines(parts, lines, 0, pos, WIDTH, "No recent messages");
		addAll(parts);
		pos += PreviewParts.logHeight() + 2 * GAP;
	}

	private void addAll(List<Gizmo> parts) {
		for (Gizmo part : parts) add(part);
	}

	private void load() {
		hide();
		String error = GameSaves.load(save);
		if (error != null) GLog.w(error);
	}

	private void toggleLock() {
		String error = GameSaves.setLocked(save, !save.locked);
		if (error != null) {
			GLog.w(error);
			return;
		}
		GameSaves.Save updated = save;
		for (GameSaves.Save candidate : GameSaves.list()) {
			if (candidate.folder.equals(save.folder)) updated = candidate;
		}
		hide();
		show(new SavePreviewWindow(updated, page, fromGameMenu));
	}

	private void delete() {
		if (save.locked) return;
		ConfirmPrompt.ask("Delete save", "Delete this save? This can't be undone.", "Delete", () -> {
			GameSaves.delete(save);
			hide();
			show(new GameSavesWindow(page, fromGameMenu));
		});
	}

	private void back() {
		hide();
		show(new GameSavesWindow(page, fromGameMenu));
	}

	@Override
	public void onBackPressed() {
		back();
	}

	@Override
	protected void reopen() {
		show(new SavePreviewWindow(save, page, fromGameMenu));
	}

	/**
	 * A button centred just above a window, outside it, like the buttons below undo mode's panel.
	 */
	static final class TopButton {

		private final Window window;
		private final String label;
		private final Runnable action;
		private TintedButton button;

		TopButton(Window window, String label, Runnable action) {
			this.window = window;
			this.label = label;
			this.action = action;
		}

		/**
		 * Adds the button. Called once, just after the window is put on screen.
		 */
		void place() {
			if (button != null || window.parent == null || window.camera == null) return;
			Camera ui = PixelScene.uiCamera;
			Camera own = window.camera;
			float top = (own.y - ui.y) / ui.zoom;
			float centre = (own.x - ui.x + own.width * own.zoom / 2f) / ui.zoom;
			button = new TintedButton(label, null, 1f, 1f, 1f, action);
			button.camera = ui;
			button.setRect(centre - TOP_BUTTON_WIDTH / 2f, Math.max(0, top - TOP_BUTTON_HEIGHT - 2), TOP_BUTTON_WIDTH, TOP_BUTTON_HEIGHT);
			Game.scene().add(button);
		}

		void remove() {
			if (button == null) return;
			button.killAndErase();
			button.destroy();
			button = null;
		}
	}

	/**
	 * Everything the hero carried in a save. Tapping an item shows its details. Back sits above the window.
	 */
	static final class SaveBackpackWindow extends EditorWindow {

		private static final float SHARE = 0.55f;

		private final Hero hero;
		private final GameSaves.Save save;
		private final int page;
		private final boolean fromGameMenu;
		private final TopButton back;

		SaveBackpackWindow(Hero hero, GameSaves.Save save, int page, boolean fromGameMenu) {
			super();
			this.hero = hero;
			this.save = save;
			this.page = page;
			this.fromGameMenu = fromGameMenu;
			back = new TopButton(this, "Back", this::back);
			addTitle("Backpack");
			float height = Math.min(PixelScene.uiCamera.height * SHARE, 180);
			List<Gizmo> slots = new ArrayList<>();
			UndoHeroView.slots(slots, UndoHeroView.belongings(hero), 0, pos, WIDTH, height);
			for (Gizmo slot : slots) add(slot);
			pos += height;
			finish();
			Game.runOnRenderThread(back::place);
		}

		private void back() {
			hide();
			show(new SavePreviewWindow(save, page, fromGameMenu));
		}

		@Override
		public void destroy() {
			back.remove();
			super.destroy();
		}

		@Override
		public void onBackPressed() {
			back();
		}

		@Override
		protected void reopen() {
			show(new SaveBackpackWindow(hero, save, page, fromGameMenu));
		}
	}
}
