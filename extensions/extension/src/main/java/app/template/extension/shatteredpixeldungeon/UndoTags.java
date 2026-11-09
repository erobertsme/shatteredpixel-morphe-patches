package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.PointerArea;
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
	private static final String KEY_MAP_VIEW = "qol_undo_map_view";
	private static final int PREVIEW_MAX = 150;
	private static final int PREVIEW_MIN = 48;
	private static final int PREVIEW_BORDER = 1;

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
			UndoScene.listen();
			if (modeOpen && UndoHistory.isOn()) buildMode(target);
		} catch (Throwable e) {
			Log.e("UndoTags", "Could not add the Undo / Redo tag", e);
		}
	}

	private static void openMode(Scene scene) {
		if (modeOpen) return;
		modeOpen = true;
		UndoHistory.resetSelection();
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

	private static void finishMode() {
		boolean load = UndoHistory.selectionPending();
		closeMode();
		if (load) UndoHistory.loadSelected();
	}

	private static void buildMode(Scene scene) {
		Camera ui = PixelScene.uiCamera;
		float top = ScreenLayout.aboveToolbar() - BUTTON_HEIGHT;

		TintedButton undo = new TintedButton("Undo", null, 1f, 1f, 1f, UndoHistory::selectPrevious);
		undo.icon(StatIcons.arrow(true));
		undo.setRect(MARGIN, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, undo);

		TintedButton done = new TintedButton("Done", null, 1f, 1f, 1f, UndoTags::finishMode);
		done.setRect((ui.width - BUTTON_WIDTH) / 2f, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, done);

		float listTop = top - BUTTON_HEIGHT - 2;
		TintedButton list = new TintedButton("History", null, 1f, 1f, 1f, () -> Game.scene().addToFront(new UndoListWindow()));
		list.setRect((ui.width - BUTTON_WIDTH) / 2f, listTop, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, list);

		TintedButton redo = new TintedButton("Redo", null, 1f, 1f, 1f, UndoHistory::selectNext);
		redo.icon(StatIcons.arrow(false));
		redo.setRect(ui.width - MARGIN - BUTTON_WIDTH, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, redo);

		Gizmo watcher = new Gizmo() {
			private String shown;
			private int shownFor = -2;
			private UndoScene shownScene;
			private boolean shownMap;
			private final List<Gizmo> details = new ArrayList<>();

			@Override
			public void update() {
				super.update();
				if (!UndoHistory.isOn() || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
					closeMode();
					return;
				}
				undo.enable(UndoHistory.canSelectPrevious());
				redo.enable(UndoHistory.canSelectNext());
				list.enable(UndoHistory.count() > 1);
				String text = UndoHistory.positionText();
				int selected = UndoHistory.selected();
				UndoScene step = UndoHistory.scene(selected);
				boolean showMap = showsMap();
				if (text.equals(shown) && selected == shownFor && step == shownScene && showMap == shownMap) return;
				shown = text;
				shownFor = selected;
				shownScene = step;
				shownMap = showMap;
				rebuild(scene, text, step, showMap);
			}

			private void rebuild(Scene scene, String text, UndoScene step, boolean showMap) {
				for (Gizmo old : details) {
					overlay.remove(old);
					old.killAndErase();
					old.destroy();
				}
				details.clear();
				float width = ui.width - 2 * MARGIN;
				float bottom = listTop - TEXT_GAP;
				if (step != null) {
					for (int k = step.lines.size() - 1; k >= 0; k--) {
						String line = step.lines.get(k);
						RenderedTextBlock block = PixelScene.renderTextBlock(UndoScene.plain(line), 6);
						block.hardlight(UndoScene.lineColor(line));
						block.maxWidth((int) width);
						block.setPos(MARGIN, bottom - block.height());
						PixelScene.align(block);
						addDetail(scene, block);
						bottom = block.top() - 1;
					}
				}
				RenderedTextBlock header = PixelScene.renderTextBlock(text, 9);
				header.hardlight(0xFFFF44);
				header.maxWidth((int) width);
				header.setPos((ui.width - header.width()) / 2f, bottom - TEXT_GAP / 2f - header.height());
				PixelScene.align(header);
				addDetail(scene, header);
				if (step == null) return;
				boolean map = showMap ? step.hasMap() : !step.hasArea() && step.hasMap();
				if (!map && !step.hasArea()) return;
				float pictureBottom = header.top() - TEXT_GAP;
				float size = Math.min(PREVIEW_MAX, Math.min(width, pictureBottom - ScreenLayout.belowStatus() - 2 * PREVIEW_BORDER));
				if (size < PREVIEW_MIN) return;
				float left = (ui.width - size) / 2f;
				float top = pictureBottom - size - PREVIEW_BORDER;
				ColorBlock frame = new ColorBlock(size + 2 * PREVIEW_BORDER, size + 2 * PREVIEW_BORDER, 0xFF000000);
				frame.x = left - PREVIEW_BORDER;
				frame.y = top - PREVIEW_BORDER;
				addDetail(scene, frame);
				List<Gizmo> picture = new ArrayList<>();
				try {
					if (map) {
						step.drawMap(picture, ui, left, top, size);
					} else {
						step.drawArea(picture, ui, left, top, size);
					}
				} catch (Throwable e) {
					Log.e("UndoTags", "Could not draw the step", e);
				}
				for (Gizmo part : picture) addDetail(scene, part);
				if (!step.hasMap() || !step.hasArea()) return;
				PointerArea toggle = new PointerArea(left, top, size, size) {
					@Override
					public void onClick(PointerEvent event) {
						CloverlordSettings.set(KEY_MAP_VIEW, !showsMap());
					}
				};
				addDetail(scene, toggle);
				RenderedTextBlock hint = PixelScene.renderTextBlock(map ? "Tap for area" : "Tap for map", 6);
				hint.hardlight(0xCCCCCC);
				hint.setPos(left + size - hint.width() - 2, top + 2);
				PixelScene.align(hint);
				addDetail(scene, hint);
			}

			private void addDetail(Scene scene, Gizmo gizmo) {
				addToOverlay(scene, gizmo);
				details.add(gizmo);
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

	private static boolean showsMap() {
		return CloverlordSettings.get(KEY_MAP_VIEW);
	}

	private static void addToOverlay(Scene scene, Gizmo gizmo) {
		gizmo.camera = PixelScene.uiCamera;
		scene.add(gizmo);
		overlay.add(gizmo);
	}
}
