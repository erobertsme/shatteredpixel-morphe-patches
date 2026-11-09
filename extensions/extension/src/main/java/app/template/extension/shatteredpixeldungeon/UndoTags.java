package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Scene;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
	private static final String[] STAT_CAPTIONS = { "STEP", "DEPTH", "TURN", "HP" };
	private static final int PANEL_PADDING = 6;
	private static final int CAPTION_HEIGHT = 8;
	private static final int VALUE_HEIGHT = 10;
	private static final int STATUS_HEIGHT = 8;
	private static final int LOG_LINES = 4;
	private static final int LOG_LINE_HEIGHT = 8;
	private static final int LOG_PADDING = 2;
	private static final int LOG_BACK_COLOR = 0x66000000;
	private static final int CAPTION_COLOR = 0x999999;
	private static final int PENDING_COLOR = 0xFFFF44;
	private static final int CURRENT_COLOR = 0x44FF44;
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
		float listTop = top - BUTTON_HEIGHT - 2;

		PointerArea blocker = new PointerArea(0, 0, ui.width, ui.height) {
			@Override
			public void onClick(PointerEvent event) {
				if (tappedMenu(event)) openGameMenu();
			}
		};
		blocker.blockLevel = PointerArea.ALWAYS_BLOCK;
		addToOverlay(scene, blocker);

		float panelLeft = MARGIN;
		float panelWidth = ui.width - 2 * MARGIN;
		float panelTop = ScreenLayout.belowStatus();
		float panelBottom = listTop - 3;
		NinePatch panel = Chrome.get(Chrome.Type.WINDOW);
		panel.x = panelLeft;
		panel.y = panelTop;
		panel.size(panelWidth, panelBottom - panelTop);
		addToOverlay(scene, panel);

		float innerLeft = panelLeft + PANEL_PADDING;
		float innerWidth = panelWidth - 2 * PANEL_PADDING;
		float y = panelTop + PANEL_PADDING;
		float column = innerWidth / STAT_CAPTIONS.length;
		for (int k = 0; k < STAT_CAPTIONS.length; k++) {
			RenderedTextBlock caption = PixelScene.renderTextBlock(STAT_CAPTIONS[k], 6);
			caption.hardlight(CAPTION_COLOR);
			caption.setPos(innerLeft + column * k + (column - caption.width()) / 2f, y);
			PixelScene.align(caption);
			addToOverlay(scene, caption);
		}
		float valuesTop = y + CAPTION_HEIGHT;
		float statusTop = valuesTop + VALUE_HEIGHT + 2;
		float logTop = statusTop + STATUS_HEIGHT + 2;
		float logHeight = LOG_LINES * LOG_LINE_HEIGHT + 2 * LOG_PADDING;
		ColorBlock logBack = new ColorBlock(innerWidth, logHeight, LOG_BACK_COLOR);
		logBack.x = innerLeft;
		logBack.y = logTop;
		addToOverlay(scene, logBack);

		float pictureTop = logTop + logHeight + 4 + PREVIEW_BORDER;
		float pictureSize = Math.min(PREVIEW_MAX, Math.min(innerWidth - 2 * PREVIEW_BORDER, panelBottom - PANEL_PADDING - PREVIEW_BORDER - pictureTop));
		boolean hasPicture = pictureSize >= PREVIEW_MIN;
		float pictureLeft = (ui.width - pictureSize) / 2f;
		if (hasPicture) {
			ColorBlock frame = new ColorBlock(pictureSize + 2 * PREVIEW_BORDER, pictureSize + 2 * PREVIEW_BORDER, 0xFF000000);
			frame.x = pictureLeft - PREVIEW_BORDER;
			frame.y = pictureTop - PREVIEW_BORDER;
			addToOverlay(scene, frame);
		}

		TintedButton undo = new TintedButton("Undo", null, 1f, 1f, 1f, UndoHistory::selectPrevious);
		undo.icon(StatIcons.arrow(true));
		undo.setRect(MARGIN, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, undo);

		TintedButton done = new TintedButton("Done", null, 1f, 1f, 1f, UndoTags::finishMode);
		done.setRect((ui.width - BUTTON_WIDTH) / 2f, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, done);

		TintedButton list = new TintedButton("History", null, 1f, 1f, 1f, () -> Game.scene().addToFront(new UndoListWindow()));
		list.setRect((ui.width - BUTTON_WIDTH) / 2f, listTop, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, list);

		TintedButton redo = new TintedButton("Redo", null, 1f, 1f, 1f, UndoHistory::selectNext);
		redo.icon(StatIcons.arrow(false));
		redo.setRect(ui.width - MARGIN - BUTTON_WIDTH, top, BUTTON_WIDTH, BUTTON_HEIGHT);
		addToOverlay(scene, redo);

		Gizmo watcher = new Gizmo() {
			private int shownFor = -2;
			private int shownCount = -1;
			private boolean shownPending;
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
				int selected = UndoHistory.selected();
				int count = UndoHistory.count();
				boolean pending = UndoHistory.selectionPending();
				UndoScene step = UndoHistory.scene(selected);
				boolean showMap = showsMap();
				if (selected == shownFor && count == shownCount && pending == shownPending && step == shownScene && showMap == shownMap) return;
				shownFor = selected;
				shownCount = count;
				shownPending = pending;
				shownScene = step;
				shownMap = showMap;
				rebuild(scene, selected, count, pending, step, showMap);
			}

			private void rebuild(Scene scene, int selected, int count, boolean pending, UndoScene step, boolean showMap) {
				for (Gizmo old : details) {
					overlay.remove(old);
					old.killAndErase();
					old.destroy();
				}
				details.clear();
				int[] stats = UndoHistory.stats(selected);
				String[] values = stats == null
					? new String[] { "-", "-", "-", "-" }
					: new String[] { (selected + 1) + "/" + count, String.valueOf(stats[0]), String.valueOf(stats[1]), stats[2] + "/" + stats[3] };
				for (int k = 0; k < values.length; k++) {
					RenderedTextBlock value = PixelScene.renderTextBlock(fit(values[k], 9, column - 2), 9);
					value.setPos(innerLeft + column * k + (column - value.width()) / 2f, valuesTop);
					PixelScene.align(value);
					addDetail(scene, value);
				}

				String status = stats == null ? "No steps kept yet" : pending ? "Not loaded - tap Done to load this step" : "You are here";
				RenderedTextBlock statusText = PixelScene.renderTextBlock(fit(status, 6, innerWidth), 6);
				statusText.hardlight(stats == null ? CAPTION_COLOR : pending ? PENDING_COLOR : CURRENT_COLOR);
				statusText.setPos((ui.width - statusText.width()) / 2f, statusTop);
				PixelScene.align(statusText);
				addDetail(scene, statusText);

				List<String> lines = step == null ? new ArrayList<>() : step.lines;
				if (lines.isEmpty()) {
					RenderedTextBlock none = PixelScene.renderTextBlock("No messages for this step", 6);
					none.hardlight(CAPTION_COLOR);
					none.setPos(innerLeft + LOG_PADDING, logTop + LOG_PADDING);
					PixelScene.align(none);
					addDetail(scene, none);
				}
				for (int k = 0; k < lines.size() && k < LOG_LINES; k++) {
					String line = lines.get(k);
					RenderedTextBlock block = PixelScene.renderTextBlock(fit(UndoScene.plain(line), 6, innerWidth - 2 * LOG_PADDING), 6);
					block.hardlight(UndoScene.lineColor(line));
					block.setPos(innerLeft + LOG_PADDING, logTop + LOG_PADDING + k * LOG_LINE_HEIGHT);
					PixelScene.align(block);
					addDetail(scene, block);
				}

				if (!hasPicture || step == null) return;
				boolean map = showMap ? step.hasMap() : !step.hasArea() && step.hasMap();
				if (!map && !step.hasArea()) return;
				List<Gizmo> picture = new ArrayList<>();
				try {
					if (map) {
						step.drawMap(picture, ui, pictureLeft, pictureTop, pictureSize);
					} else {
						step.drawArea(picture, ui, pictureLeft, pictureTop, pictureSize);
					}
				} catch (Throwable e) {
					Log.e("UndoTags", "Could not draw the step", e);
				}
				for (Gizmo part : picture) addDetail(scene, part);
				if (!step.hasMap() || !step.hasArea()) return;
				PointerArea toggle = new PointerArea(pictureLeft, pictureTop, pictureSize, pictureSize) {
					@Override
					public void onClick(PointerEvent event) {
						toggleMap();
					}
				};
				addDetail(scene, toggle);
				RenderedTextBlock hint = PixelScene.renderTextBlock(map ? "Tap for area" : "Tap for map", 6);
				hint.hardlight(0xCCCCCC);
				hint.setPos(pictureLeft + pictureSize - hint.width() - 2, pictureTop + 2);
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
	 * [text] shortened with ".." until it fits in [width] at [size].
	 */
	static String fit(String text, int size, float width) {
		RenderedTextBlock probe = PixelScene.renderTextBlock(text, size);
		float full = probe.width();
		probe.destroy();
		if (full <= width) return text;
		int keep = Math.max(1, (int) (text.length() * width / full));
		while (keep > 1) {
			String shorter = text.substring(0, keep).trim() + "..";
			RenderedTextBlock test = PixelScene.renderTextBlock(shorter, size);
			float measured = test.width();
			test.destroy();
			if (measured <= width) return shorter;
			keep--;
		}
		return "..";
	}

	private static boolean tappedMenu(PointerEvent event) {
		try {
			Component button = menuButton();
			if (button == null || event == null) return false;
			PointF point = PixelScene.uiCamera.screenToCamera((int) event.current.x, (int) event.current.y);
			return point.x >= button.left() && point.x <= button.right() && point.y >= button.top() && point.y <= button.bottom();
		} catch (Throwable e) {
			return false;
		}
	}

	private static Component menuButton() throws ReflectiveOperationException {
		Field sceneField = GameScene.class.getDeclaredField("scene");
		sceneField.setAccessible(true);
		Object scene = sceneField.get(null);
		if (scene == null) return null;
		Field menuField = GameScene.class.getDeclaredField("menu");
		menuField.setAccessible(true);
		Object menu = menuField.get(scene);
		if (menu == null) return null;
		Field buttonField = menu.getClass().getDeclaredField("btnMenu");
		buttonField.setAccessible(true);
		return (Component) buttonField.get(menu);
	}

	/**
	 * Closes undo mode and opens the game menu, as the menu button would.
	 */
	private static void openGameMenu() {
		closeMode();
		try {
			Component button = menuButton();
			if (button == null) return;
			for (Class<?> type = button.getClass(); type != null; type = type.getSuperclass()) {
				try {
					Method click = type.getDeclaredMethod("onClick");
					click.setAccessible(true);
					click.invoke(button);
					return;
				} catch (NoSuchMethodException ignored) {
				}
			}
		} catch (Throwable e) {
			Log.e("UndoTags", "Could not open the game menu", e);
		}
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

	static boolean showsMap() {
		return CloverlordSettings.get(KEY_MAP_VIEW);
	}

	static void toggleMap() {
		CloverlordSettings.set(KEY_MAP_VIEW, !showsMap());
	}

	private static void addToOverlay(Scene scene, Gizmo gizmo) {
		gizmo.camera = PixelScene.uiCamera;
		scene.add(gizmo);
		overlay.add(gizmo);
	}
}
