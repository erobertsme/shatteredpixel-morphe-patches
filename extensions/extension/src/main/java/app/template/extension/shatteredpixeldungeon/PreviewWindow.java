package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A saved moment of a game shown the way undo mode shows a step: the hero at the time with their talents and backpack, the depth and turn,
 * the last game messages and the dungeon around them. Subclasses add their own buttons. In landscape the picture sits beside the rest.
 */
abstract class PreviewWindow extends EditorWindow {

	private static final int STATS_WIDTH = 40;
	private static final int LINK_WIDTH = 60;
	private static final int LINK_UNDERLINE_GAP = 1;
	private static final int LINK_LABEL_GAP = 2;
	private static final int TALENTS_WIDTH = 56;
	private static final int LINK_TAP_MARGIN = 3;
	private static final int BACKPACK_WIDTH = 60;
	private static final int BACKPACK_HEIGHT = 16;
	static final int TOP_BUTTON_WIDTH = 50;
	static final int TOP_BUTTON_HEIGHT = 20;

	private File picture;

	/**
	 * Lays out the preview of a Game Save or undo step, with its picture of the dungeon when it has one.
	 */
	protected void layout(GameSaves.Save save) {
		layout(save.title(), save);
	}

	/**
	 * Like the other, under [title] instead of the save's own.
	 */
	protected void layout(String title, GameSaves.Save save) {
		picture = save.picture();
		layout(title, save.preview(), save.hero());
	}

	/**
	 * Whether the preview shows the last game messages.
	 */
	protected boolean showsLog() {
		return true;
	}

	/**
	 * Lays out the preview of [scene] and [hero] under [title], then the buttons from [addActions], and sizes the window.
	 */
	protected void layout(String title, UndoScene scene, Hero hero) {
		addTitle(title);
		float columnTop = pos;
		boolean hasPicture = scene != null && (scene.hasArea() || scene.hasMap());
		boolean wide = PixelScene.landscape() && hasPicture;
		addHero(hero, scene);
		if (hasPicture) {
			if (showsLog()) addLog(scene.lines);
		} else {
			addHint("No picture for this one. Ones made from now on keep one.");
		}
		float pictureBottom = pos;
		if (hasPicture && !wide) pictureBottom = addPicture(scene, 0, pos);
		if (wide) pictureBottom = addPicture(scene, WIDTH + 2 * GAP, columnTop);
		addActions();
		resize(wide ? 2 * WIDTH + 2 * GAP : WIDTH, (int) Math.max(pos, pictureBottom));
	}

	/**
	 * Adds the window's buttons at pos and moves pos past them.
	 */
	protected abstract void addActions();

	/**
	 * More small lines under the depth and turn, beside the hero.
	 */
	protected String[] extraStats() {
		return new String[0];
	}

	/**
	 * A line under the hero whose value can be tapped, or null for none.
	 */
	protected StatLink statLink() {
		return null;
	}

	/**
	 * A stat whose value is a link, on one line under the hero: a dim [label], then [text] in yellow and underlined, which runs [action] when
	 * tapped.
	 */
	static final class StatLink {
		final String label;
		final String text;
		final Runnable action;

		StatLink(String label, String text, Runnable action) {
			this.label = label;
			this.text = text;
			this.action = action;
		}
	}

	/**
	 * Adds two buttons side by side on one row.
	 */
	protected void addButtonPair(String left, Runnable leftAction, String right, Runnable rightAction) {
		float half = (WIDTH - GAP) / 2f;
		addButton(left, 0, half, leftAction);
		addButton(right, half + GAP, half, rightAction);
		pos += ROW_HEIGHT + GAP;
	}

	private void addHero(Hero hero, UndoScene scene) {
		float top = pos;
		List<String> lines = new ArrayList<>();
		lines.add("Depth " + (scene != null && scene.depth >= 0 ? scene.depth : "-"));
		lines.add("Turn " + (scene != null && scene.turn >= 0 ? scene.turn : "-"));
		Collections.addAll(lines, extraStats());
		List<Gizmo> parts = new ArrayList<>();
		PreviewParts.statLines(parts, lines.toArray(new String[0]), WIDTH, top, LINK_WIDTH);
		float statsWidth = STATS_WIDTH;
		for (Gizmo part : parts) {
			if (part instanceof RenderedTextBlock) statsWidth = Math.max(statsWidth, ((RenderedTextBlock) part).width());
		}
		float statsBottom = top + lines.size() * PreviewParts.STAT_LINE_HEIGHT;
		if (hero != null) {
			try {
				UndoHeroView.build(parts, hero, 0, top, WIDTH - statsWidth - 4);
			} catch (Throwable e) {
				Log.e("PreviewWindow", "Could not show the hero", e);
			}
			statsBottom = Math.max(statsBottom, top + UndoHeroView.HEIGHT);
		}
		addAll(parts);
		pos = statsBottom + 3;
		StatLink link = statLink();
		if (link != null) pos = addLink(link, pos) + GAP;
		if (hero == null) return;
		float talentsLeft = BACKPACK_WIDTH + 2;
		addHeroButton("Backpack", Icons.get(Icons.BACKPACK), 0, BACKPACK_WIDTH, () -> {
			hide();
			show(new BackpackWindow(hero, PreviewWindow.this::reopen));
		});
		addHeroButton("Talents", Icons.get(Icons.TALENT), talentsLeft, TALENTS_WIDTH, () -> UndoHeroView.showTalents(hero));
		pos += BACKPACK_HEIGHT + 2 * GAP;
	}

	/**
	 * A button beside the hero, on the row at pos.
	 */
	private void addHeroButton(String label, Image icon, float x, float width, Runnable action) {
		RedButton button = new RedButton(label, 7) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		button.icon(icon);
		button.setRect(x, pos, width, BACKPACK_HEIGHT);
		add(button);
	}

	/**
	 * Adds [link] as one line at [y], ending at the window's right edge: its dim label, then its text in yellow and underlined like the
	 * game's own links, with a tap area over the text. Returns the line's bottom.
	 */
	private float addLink(StatLink link, float y) {
		RenderedTextBlock text = PixelScene.renderTextBlock(link.text, 6);
		text.hardlight(TITLE_COLOR);
		text.setPos(WIDTH - text.width(), y);
		PixelScene.align(text);
		add(text);
		RenderedTextBlock label = PixelScene.renderTextBlock(link.label, 6);
		label.hardlight(PreviewParts.DIM_COLOR);
		label.setPos(text.left() - LINK_LABEL_GAP - label.width(), y);
		PixelScene.align(label);
		add(label);
		ColorBlock underline = new ColorBlock(text.width(), 1, 0xFF000000 | TITLE_COLOR);
		underline.x = text.left();
		underline.y = text.bottom() + LINK_UNDERLINE_GAP;
		add(underline);
		add(new PointerArea(text.left() - LINK_TAP_MARGIN, y - LINK_TAP_MARGIN, text.width() + 2 * LINK_TAP_MARGIN,
			text.height() + 2 * LINK_TAP_MARGIN) {
			@Override
			public void onClick(PointerEvent event) {
				link.action.run();
			}
		});
		return underline.y + 1;
	}

	/**
	 * Adds the picture with its top left corner at [x], [y]. Returns the bottom of the picture. Below the rest, it also moves pos past it.
	 */
	private float addPicture(UndoScene scene, float x, float y) {
		float size = WIDTH - 2 * PreviewParts.BORDER;
		List<Gizmo> parts = new ArrayList<>();
		PreviewParts.frame(parts, x + PreviewParts.BORDER, y + PreviewParts.BORDER, size);
		PreviewParts.picture(parts, scene, picture, null, x + PreviewParts.BORDER, y + PreviewParts.BORDER, size, this::refresh);
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

	/**
	 * A button centered just above a window, outside it, like the buttons below undo mode's panel.
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
			float center = (own.x - ui.x + own.width * own.zoom / 2f) / ui.zoom;
			button = new TintedButton(label, null, 1f, 1f, 1f, action);
			button.camera = ui;
			button.setRect(center - TOP_BUTTON_WIDTH / 2f, Math.max(0, top - TOP_BUTTON_HEIGHT - 2), TOP_BUTTON_WIDTH, TOP_BUTTON_HEIGHT);
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
	 * Everything the hero carried. Tapping an item shows its details. Back sits above the window and runs [onBack].
	 */
	static final class BackpackWindow extends EditorWindow {

		private static final float SHARE = 0.55f;

		private final Hero hero;
		private final Runnable onBack;
		private final TopButton back;

		BackpackWindow(Hero hero, Runnable onBack) {
			super();
			this.hero = hero;
			this.onBack = onBack;
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
			onBack.run();
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
			show(new BackpackWindow(hero, onBack));
		}
	}
}
