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
	private static final long HOLD_MS = 250;
	private static final int HOLD_VIBRATION_MS = 20;
	private static final float TAP_SLOP = 6;

	private static final List<Gizmo> parts = new ArrayList<>();
	private static Runnable onClose;

	/**
	 * One tag in the editor: a tag added by the patches, or with [gameIndex] at 0 or more, the game tag it stands for.
	 */
	private static final class Entry {
		final String name;
		final TagPlacement placement;
		final ScreenTag screenTag;
		final int gameIndex;

		Entry(ScreenTag screenTag) {
			this.name = screenTag.name();
			this.placement = screenTag.placement;
			this.screenTag = screenTag;
			this.gameIndex = -1;
		}

		Entry(int gameIndex) {
			this.name = GameTags.NAMES[gameIndex];
			this.placement = GameTags.PLACEMENTS[gameIndex];
			this.screenTag = null;
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
		for (ScreenTag tag : ScreenTags.enabled()) entries.add(new Entry(tag));
		if (!QolSettings.isAvailable()) return entries;
		for (int i = 0; i < GameTags.NAMES.length; i++) entries.add(new Entry(i));
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
		RenderedTextBlock hint = PixelScene.renderTextBlock("Tap a tag for all of its settings. Press and hold a tag briefly, then drag it along its edge or onto another edge.", 6);
		hint.maxWidth((int) (ui.width - insets[0] - insets[2] - 2 * MARGIN - HINT_SIDE_SPACE));
		hint.setPos((ui.width - hint.width()) / 2f, insets[1] + HINT_TOP);
		PixelScene.align(hint);
		add(scene, hint);

		if (QolSettings.isAvailable()) add(scene, GameTagGhosts.danger());
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
			placement.place(tag, rect);
			visual = tag;
			RenderedTextBlock name = PixelScene.renderTextBlock(entry.name, 6);
			name.hardlight(LABEL_COLOR);
			label = name;
			placeLabel(name, rect);
		}
		add(scene, visual);
		add(scene, label);

		add(scene, new PointerArea(rect[0], rect[1], rect[2], rect[3]) {
			private long downAt;
			private PointF downPoint;
			private boolean pressed;
			private boolean dragging;
			private boolean canceled;

			@Override
			public void onPointerDown(PointerEvent event) {
				downAt = System.currentTimeMillis();
				downPoint = ScreenLayout.toUi(event);
				pressed = true;
				dragging = false;
				canceled = false;
			}

			@Override
			public void update() {
				super.update();
				if (!pressed || dragging || canceled || System.currentTimeMillis() - downAt < HOLD_MS) return;
				dragging = true;
				visual.flash();
				Game.vibrate(HOLD_VIBRATION_MS);
			}

			@Override
			public void onDrag(PointerEvent event) {
				PointF point = ScreenLayout.toUi(event);
				if (!dragging) {
					if (Math.abs(point.x - downPoint.x) > TAP_SLOP || Math.abs(point.y - downPoint.y) > TAP_SLOP) canceled = true;
					return;
				}
				TagPlacement.Side side = nearestSide(point, placement.sides());
				if (placement.isAuto() || side != placement.side()) placement.side(side);
				float[] now = placement.rect(0);
				float length = side == TagPlacement.Side.TOP ? now[2] : now[3];
				placement.offset(Math.round(placement.offsetAt(point.x, point.y) - length / 2f));
				float[] placed = placement.rect(0);
				visual.setRect(placed[0], placed[1], placed[2], placed[3]);
				visual.flip(side == TagPlacement.Side.LEFT);
				if (visual instanceof IconTag) ((IconTag) visual).onTop(side == TagPlacement.Side.TOP);
				placeLabel((RenderedTextBlock) label, placed);
			}

			@Override
			public void onPointerUp(PointerEvent event) {
				pressed = false;
				if (dragging) Game.runOnRenderThread(TagLayoutEditor::build);
			}

			@Override
			public void onClick(PointerEvent event) {
				if (!dragging && !canceled) openSettings(entry);
			}
		});
	}

	/**
	 * Puts a tag's name under it.
	 */
	private static void placeLabel(RenderedTextBlock label, float[] rect) {
		float x = rect[0] + (rect[2] - label.width()) / 2f;
		float y = rect[1] + rect[3] + 1;
		label.setPos(Math.max(0, Math.min(PixelScene.uiCamera.width - label.width(), x)), y);
		PixelScene.align(label);
	}

	/**
	 * Where a tag sits now: its own position, or for one that keeps its automatic place, where that is.
	 */
	private static float[] rectOf(Entry entry) {
		return entry.isGameTag() ? GameTags.rects()[entry.gameIndex] : entry.screenTag.rect();
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
		EditorWindow.show(entry.screenTag == null
			? new TagSettingsWindow(entry.placement, entry.name, TagLayoutEditor::build)
			: new TagSettingsWindow(entry.screenTag, entry.name, TagLayoutEditor::build));
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
