package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
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
 * Shows every tag on the screen at once, with the game's own tags as outlines, so each can be dragged along its edge or onto another edge.
 * Changes apply straight away. On the main menu a stand-in game screen sits behind them.
 */
final class TagLayoutEditor {

	private static final int DIM_COLOR = 0xAA000000;
	private static final int LABEL_COLOR = 0xFFFF44;
	private static final int BUTTON_WIDTH = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int MARGIN = 4;

	private static final List<Gizmo> parts = new ArrayList<>();
	private static Runnable onClose;

	private static final class Entry {
		final String name;
		final TagPlacement placement;

		Entry(String name, TagPlacement placement) {
			this.name = name;
			this.placement = placement;
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
		if (UndoHistory.isAvailable()) entries.add(new Entry("Undo / Redo", UndoTags.PLACEMENT));
		if (QolSettings.isAvailable()) {
			entries.add(new Entry("Attack Tile", ForceAttack.PLACEMENT));
			for (int id : CustomItemTags.ids()) entries.add(new Entry(CustomItemTags.name(id), CustomItemTags.placement(id)));
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
		GameTagGhosts.show();

		for (Entry entry : entries()) addTag(scene, entry);

		float[] insets = ScreenLayout.insets();
		float top = insets[1] + 40;
		RenderedTextBlock hint = PixelScene.renderTextBlock("Drag a tag along its edge, or onto another edge. Outlines show where the game's own buttons appear.", 6);
		hint.maxWidth((int) (ui.width - insets[0] - insets[2] - 2 * MARGIN - 40));
		hint.setPos((ui.width - hint.width()) / 2f, top);
		PixelScene.align(hint);
		add(scene, hint);

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
		IconTag tag = new IconTag(() -> { });
		float[] rect = rectOf(placement);
		tag.setRect(rect[0], rect[1], rect[2], rect[3]);
		tag.onTop(!placement.isAuto() && placement.side() == TagPlacement.Side.TOP);
		tag.flip(!placement.isAuto() && placement.side() == TagPlacement.Side.LEFT);
		tag.style(placement);
		add(scene, tag);

		RenderedTextBlock label = PixelScene.renderTextBlock(entry.name, 6);
		label.hardlight(LABEL_COLOR);
		float labelX = rect[0] + (rect[2] - label.width()) / 2f;
		labelX = Math.max(0, Math.min(PixelScene.uiCamera.width - label.width(), labelX));
		label.setPos(labelX, rect[1] + rect[3] + 1);
		PixelScene.align(label);
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
				TagPlacement.Side side = nearestSide(point);
				if (placement.isAuto() || side != placement.side()) placement.side(side);
				float[] now = placement.rect(0);
				float length = side == TagPlacement.Side.TOP ? now[2] : now[3];
				placement.offset(Math.round(placement.offsetAt(point.x, point.y) - length / 2f));
				float[] placed = placement.rect(0);
				tag.onTop(side == TagPlacement.Side.TOP);
				tag.flip(side == TagPlacement.Side.LEFT);
				tag.setRect(placed[0], placed[1], placed[2], placed[3]);
				label.setPos(Math.max(0, Math.min(PixelScene.uiCamera.width - label.width(), placed[0] + (placed[2] - label.width()) / 2f)), placed[1] + placed[3] + 1);
				moved = true;
			}

			@Override
			public void onPointerUp(PointerEvent event) {
				if (moved) Game.runOnRenderThread(TagLayoutEditor::build);
			}
		});
	}

	/**
	 * Where a tag sits now: its own position, or for a tag that keeps its default place, next to the game's own tags.
	 */
	private static float[] rectOf(TagPlacement placement) {
		if (!placement.isAuto()) return placement.rect(0);
		float[] live = placement == ForceAttack.PLACEMENT ? ForceAttack.autoRect() : null;
		if (live != null) return live;
		float[] insets = ScreenLayout.insets();
		float width = placement.tagWidth();
		float height = placement.tagHeight();
		return new float[] { PixelScene.uiCamera.width - insets[2] - width, ScreenLayout.aboveToolbar() - height, width, height };
	}

	/**
	 * The edge of the usable screen closest to [point]. Tags can sit on the left, right or top edge.
	 */
	private static TagPlacement.Side nearestSide(PointF point) {
		Camera ui = PixelScene.uiCamera;
		float[] insets = ScreenLayout.insets();
		float left = point.x - insets[0];
		float right = ui.width - insets[2] - point.x;
		float top = point.y - insets[1];
		if (top < left && top < right) return TagPlacement.Side.TOP;
		return left <= right ? TagPlacement.Side.LEFT : TagPlacement.Side.RIGHT;
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
