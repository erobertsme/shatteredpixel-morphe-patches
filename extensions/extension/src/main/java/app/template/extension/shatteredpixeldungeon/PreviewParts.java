package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.PointerArea;

import java.util.List;

/**
 * The pieces shared by undo mode and the game save preview: the small stat lines, the box of game messages and the picture of the dungeon.
 * Each adds its parts to a list, so the caller decides whether they go into a window or onto the screen.
 */
final class PreviewParts {

	static final int LOG_LINES = 4;
	static final int LOG_LINE_HEIGHT = 8;
	static final int LOG_PADDING = 2;
	static final int STAT_LINE_HEIGHT = 8;
	static final int BORDER = 1;
	static final int DIM_COLOR = 0x999999;
	private static final int LOG_BACK_COLOR = 0x66000000;
	private static final int FRAME_COLOR = 0xFF000000;
	private static final int HINT_COLOR = 0xCCCCCC;
	private static final int TEXT_SIZE = 6;

	private PreviewParts() {
	}

	/**
	 * [text] shortened with ".." until it fits in [width] at [size].
	 */
	static String fit(String text, int size, float width) {
		float full = measure(text, size);
		if (full <= width) return text;
		int keep = Math.max(1, (int) (text.length() * width / full));
		while (keep > 1) {
			String shorter = text.substring(0, keep).trim() + "..";
			if (measure(shorter, size) <= width) return shorter;
			keep--;
		}
		return "..";
	}

	private static float measure(String text, int size) {
		RenderedTextBlock probe = PixelScene.renderTextBlock(text, size);
		float width = probe.width();
		probe.destroy();
		return width;
	}

	/**
	 * A line of small text at [x], [y], cut short to fit [width].
	 */
	static RenderedTextBlock line(List<Gizmo> out, String text, int color, float x, float y, float width) {
		RenderedTextBlock block = PixelScene.renderTextBlock(fit(text, TEXT_SIZE, width), TEXT_SIZE);
		block.hardlight(color);
		block.setPos(x, y);
		PixelScene.align(block);
		out.add(block);
		return block;
	}

	/**
	 * Small dim lines stacked downwards from [y], each ending at [right].
	 */
	static void statLines(List<Gizmo> out, String[] lines, float right, float y, float width) {
		for (String text : lines) {
			RenderedTextBlock block = line(out, text, DIM_COLOR, 0, y, width);
			block.setPos(right - block.width(), y);
			PixelScene.align(block);
			y += STAT_LINE_HEIGHT;
		}
	}

	static float logHeight() {
		return LOG_LINES * LOG_LINE_HEIGHT + 2 * LOG_PADDING;
	}

	static void block(List<Gizmo> out, float x, float y, float width, float height, int color) {
		ColorBlock block = new ColorBlock(width, height, color);
		block.x = x;
		block.y = y;
		out.add(block);
	}

	static void logBackground(List<Gizmo> out, float x, float y, float width) {
		block(out, x, y, width, logHeight(), LOG_BACK_COLOR);
	}

	/**
	 * Up to four game messages in their own colours, one per line, or [empty] when there are none.
	 */
	static void logLines(List<Gizmo> out, List<String> lines, float x, float y, float width, String empty) {
		float inner = width - 2 * LOG_PADDING;
		if (lines.isEmpty()) line(out, empty, DIM_COLOR, x + LOG_PADDING, y + LOG_PADDING, inner);
		for (int k = 0; k < lines.size() && k < LOG_LINES; k++) {
			String text = lines.get(k);
			line(out, UndoScene.plain(text), UndoScene.lineColor(text), x + LOG_PADDING, y + LOG_PADDING + k * LOG_LINE_HEIGHT, inner);
		}
	}

	/**
	 * The black frame around a [size] picture whose top left corner is at [x], [y].
	 */
	static void frame(List<Gizmo> out, float x, float y, float size) {
		block(out, x - BORDER, y - BORDER, size + 2 * BORDER, size + 2 * BORDER, FRAME_COLOR);
	}

	/**
	 * The area view or the map of [scene], whichever was last chosen, with a tap on it switching between the two and then running
	 * [onToggle].
	 */
	static void picture(List<Gizmo> out, UndoScene scene, Camera camera, float x, float y, float size, Runnable onToggle) {
		boolean map = UndoTags.showsMap() ? scene.hasMap() : !scene.hasArea() && scene.hasMap();
		if (!map && !scene.hasArea()) return;
		try {
			if (map) {
				scene.drawMap(out, camera, x, y, size);
			} else {
				scene.drawArea(out, camera, x, y, size);
			}
		} catch (Throwable e) {
			Log.e("PreviewParts", "Could not draw the dungeon", e);
		}
		if (!scene.hasMap() || !scene.hasArea()) return;
		out.add(new PointerArea(x, y, size, size) {
			@Override
			public void onClick(PointerEvent event) {
				UndoTags.toggleMap();
				onToggle.run();
			}
		});
		RenderedTextBlock hint = PixelScene.renderTextBlock(map ? "Tap for area" : "Tap for map", TEXT_SIZE);
		hint.hardlight(HINT_COLOR);
		hint.setPos(x + size - hint.width() - 2, y + 2);
		PixelScene.align(hint);
		out.add(hint);
	}
}
