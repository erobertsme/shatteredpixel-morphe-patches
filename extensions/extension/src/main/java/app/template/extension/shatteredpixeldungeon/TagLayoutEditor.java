package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Scene;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * The tags editor: every tag on the screen at once, the game's own included, so each can be dragged along its edge or onto another edge,
 * or tapped for all of its settings. Changes apply straight away. On the main menu a stand-in game screen sits behind them.
 */
final class TagLayoutEditor {

	private static final int DIM_COLOR = 0xAA000000;
	private static final int LABEL_COLOR = 0xFFFF44;
	private static final int BUTTON_WIDTH = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int MARGIN = 4;
	private static final int HINT_TOP = 40;
	private static final int HINT_SIDE_SPACE = 40;

	private static final List<Gizmo> parts = new ArrayList<>();
	private static Runnable onClose;

	/**
	 * One tag in the editor. [gameIndex] is the game tag it stands for, or -1 for a tag added by the patches.
	 */
	private static final class Entry {
		final String name;
		final TagPlacement placement;
		final int gameIndex;

		Entry(String name, TagPlacement placement, int gameIndex) {
			this.name = name;
			this.placement = placement;
			this.gameIndex = gameIndex;
		}

		boolean isGameTag() {
			return gameIndex >= 0;
		}
	}

	private TagLayoutEditor() {
	}

	/**
	 * Opens the editor over the current screen. [whenDone] runs after Done.
	 */
	static void open(Runnable whenDone) {
		onClose = whenDone;
		build();
	}

	private static List<Entry> entries() {
		List<Entry> entries = new ArrayList<>();
		if (UndoHistory.isAvailable()) entries.add(new Entry("Undo / Redo", UndoTags.PLACEMENT, -1));
		if (QolSettings.isAvailable()) {
			entries.add(new Entry("Attack Tile", ForceAttack.PLACEMENT, -1));
			for (int id : CustomItemTags.ids()) entries.add(new Entry(CustomItemTags.name(id), CustomItemTags.placement(id), -1));
			for (int i = 0; i < GameTags.NAMES.length; i++) entries.add(new Entry(GameTags.NAMES[i], GameTags.PLACEMENTS[i], i));
		}
		return entries;
	}

	private static void build() {
		clear();
		Scene scene = Game.scene();
		if (scene == null) return;
		Camera ui = PixelScene.uiCamera;
		boolean inGame = scene instanceof GameScene;
		if (!inGame) MenuPreview.show(false);

		PointerArea blocker = new PointerArea(0, 0, ui.width, ui.height);
		blocker.blockLevel = PointerArea.ALWAYS_BLOCK;
		add(scene, blocker);
		if (inGame) add(scene, new ColorBlock(ui.width, ui.height, DIM_COLOR));

		float[] insets = ScreenLayout.insets();
		RenderedTextBlock hint = PixelScene.renderTextBlock("Drag a tag along its edge or onto another edge. Tap a tag for all of its settings.", 6);
		hint.maxWidth((int) (ui.width - insets[0] - insets[2] - 2 * MARGIN - HINT_SIDE_SPACE));
		hint.setPos((ui.width - hint.width()) / 2f, insets[1] + HINT_TOP);
		PixelScene.align(hint);
		add(scene, hint);

		for (Entry entry : entries()) addTag(scene, entry);

		float buttons = ScreenLayout.aboveToolbar() - BUTTON_HEIGHT;
		add(scene, button("Reset", (ui.width - MARGIN) / 2f - BUTTON_WIDTH, buttons, TagLayoutEditor::reset));
		add(scene, button("Done", (ui.width + MARGIN) / 2f, buttons, TagLayoutEditor::close));
	}

	private static TintedButton button(String label, float x, float y, Runnable action) {
		TintedButton button = new TintedButton(label, null, 1f, 1f, 1f, action);
		button.setRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
		return button;
	}

	private static void addTag(Scene scene, Entry entry) {
		TagPlacement placement = entry.placement;
		float[] rect = rectOf(entry);
		Tag visual;
		Gizmo label;
		if (entry.isGameTag()) {
			Gizmo[] ghost = GameTagGhosts.ghost(entry.gameIndex, rect);
			visual = (Tag) ghost[0];
			label = ghost[1];
		} else {
			IconTag tag = new IconTag(() -> { });
			tag.setRect(rect[0], rect[1], rect[2], rect[3]);
			tag.onTop(!placement.isAuto() && placement.side() == TagPlacement.Side.TOP);
			tag.flip(!placement.isAuto() && placement.side() == TagPlacement.Side.LEFT);
			tag.style(placement);
			visual = tag;
			RenderedTextBlock name = PixelScene.renderTextBlock(entry.name, 6);
			name.hardlight(LABEL_COLOR);
			label = name;
			placeLabel(entry, name, rect);
		}
		add(scene, visual);
		add(scene, label);

		add(scene, new PointerArea(rect[0], rect[1], rect[2], rect[3]) {
			private boolean moved;

			@Override
			public void onPointerDown(PointerEvent event) {
				moved = false;
			}

			@Override
			public void onDrag(PointerEvent event) {
				PointF point = PixelScene.uiCamera.screenToCamera((int) event.current.x, (int) event.current.y);
				TagPlacement.Side side = nearestSide(point, placement.sides());
				if (placement.isAuto() || side != placement.side()) placement.side(side);
				float[] now = placement.rect(0);
				float length = side == TagPlacement.Side.TOP ? now[2] : now[3];
				placement.offset(Math.round(placement.offsetAt(point.x, point.y) - length / 2f));
				float[] placed = placement.rect(0);
				visual.setRect(placed[0], placed[1], placed[2], placed[3]);
				visual.flip(side == TagPlacement.Side.LEFT);
				if (visual instanceof IconTag) ((IconTag) visual).onTop(side == TagPlacement.Side.TOP);
				placeLabel(entry, (RenderedTextBlock) label, placed);
				moved = true;
			}

			@Override
			public void onPointerUp(PointerEvent event) {
				if (moved) Game.runOnRenderThread(TagLayoutEditor::build);
			}

			@Override
			public void onClick(PointerEvent event) {
				if (!moved) openSettings(entry);
			}
		});
	}

	/**
	 * Puts a tag's name under it, or for a game tag, on it.
	 */
	private static void placeLabel(Entry entry, RenderedTextBlock label, float[] rect) {
		float x = rect[0] + (rect[2] - label.width()) / 2f;
		float y = entry.isGameTag() ? rect[1] + (rect[3] - label.height()) / 2f : rect[1] + rect[3] + 1;
		label.setPos(Math.max(0, Math.min(PixelScene.uiCamera.width - label.width(), x)), y);
		PixelScene.align(label);
	}

	/**
	 * Where a tag sits now: its own position, or for one that keeps its automatic place, where that is.
	 */
	private static float[] rectOf(Entry entry) {
		if (entry.isGameTag()) return GameTags.rects()[entry.gameIndex];
		TagPlacement placement = entry.placement;
		if (!placement.isAuto()) return placement.rect(0);
		float[] live = placement == ForceAttack.PLACEMENT ? ForceAttack.autoRect() : null;
		if (live != null) return live;
		float[] insets = ScreenLayout.insets();
		float width = placement.tagWidth();
		float height = placement.tagHeight();
		return new float[] { PixelScene.uiCamera.width - insets[2] - width, ScreenLayout.aboveToolbar() - height, width, height };
	}

	/**
	 * The edge of the usable screen closest to [point], out of [allowed].
	 */
	private static TagPlacement.Side nearestSide(PointF point, TagPlacement.Side[] allowed) {
		Camera ui = PixelScene.uiCamera;
		float[] insets = ScreenLayout.insets();
		TagPlacement.Side best = allowed[0];
		float bestDistance = Float.MAX_VALUE;
		for (TagPlacement.Side side : allowed) {
			float distance = side == TagPlacement.Side.TOP ? point.y - insets[1]
				: side == TagPlacement.Side.LEFT ? point.x - insets[0]
				: ui.width - insets[2] - point.x;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = side;
			}
		}
		return best;
	}

	/**
	 * Opens a tag's full settings, coming back to the editor afterwards.
	 */
	private static void openSettings(Entry entry) {
		clear();
		EditorWindow.show(new TagSettingsWindow(entry.placement, entry.name, TagLayoutEditor::build));
	}

	private static void reset() {
		ConfirmPrompt.ask("Reset tags", "Put every tag back in its default place?", "Reset", () -> {
			for (Entry entry : entries()) entry.placement.resetPosition();
			build();
		});
	}

	private static void close() {
		clear();
		Runnable after = onClose;
		onClose = null;
		if (after != null) after.run();
	}

	private static void clear() {
		GameTagGhosts.hide();
		MenuPreview.hide();
		for (Gizmo gizmo : parts) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		parts.clear();
	}

	private static void add(Scene scene, Gizmo gizmo) {
		gizmo.camera = PixelScene.uiCamera;
		scene.add(gizmo);
		parts.add(gizmo);
	}
}
