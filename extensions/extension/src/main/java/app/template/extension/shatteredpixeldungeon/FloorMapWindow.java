package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * The map of a floor the hero has been to, to choose the tile to teleport to. Another floor's map is read from its save, so looking at it
 * neither takes the hero there nor changes anything on it.
 */
public class FloorMapWindow extends EditorWindow {

	private static final String LEVEL = "level";
	private static final int OUTLINE_COLOR = 0xFFFFFFFF;
	private static final int OUTLINE = 1;
	private static final int BACKGROUND_COLOR = 0xFF000000;

	private final String title;
	private final int depth;
	private final int branch;
	private final UndoScene map;
	private final float mapTop;
	private final List<ColorBlock> outline = new ArrayList<>();
	private final RedButton teleport;
	private int chosen = -1;

	/**
	 * Shows the map of [depth] of [branch] to choose a tile on, or says why it can't be shown.
	 */
	static void open(String title, int depth, int branch) {
		UndoScene map = load(depth, branch);
		if (map == null) {
			show(new WndMessage("The map of floor " + depth + " couldn't be read."));
			return;
		}
		show(new FloorMapWindow(title, depth, branch, map));
	}

	private static UndoScene load(int depth, int branch) {
		try {
			if (depth == Dungeon.depth && branch == Dungeon.branch) return UndoScene.floorMap(Dungeon.hero, Dungeon.level);
			Bundle saved = FileUtils.bundleFromFile(GamesInProgress.depthFile(GamesInProgress.curSlot, depth, branch));
			Bundle level = saved == null ? null : saved.getBundle(LEVEL);
			return level == null ? null : UndoScene.savedFloorMap(level);
		} catch (Throwable e) {
			Log.e("FloorMapWindow", "Could not read floor " + depth, e);
			return null;
		}
	}

	private FloorMapWindow(String title, int depth, int branch, UndoScene map) {
		super();
		this.title = title;
		this.depth = depth;
		this.branch = branch;
		this.map = map;
		addTitle(title);
		addHint("Tap where you want to appear.");
		mapTop = pos;
		List<Gizmo> parts = new ArrayList<>();
		ColorBlock background = new ColorBlock(WIDTH, WIDTH, BACKGROUND_COLOR);
		background.y = mapTop;
		parts.add(background);
		map.drawMap(parts, null, 0, mapTop, WIDTH);
		for (Gizmo part : parts) add(part);
		for (int side = 0; side < 4; side++) {
			ColorBlock line = new ColorBlock(1, 1, OUTLINE_COLOR);
			line.visible = false;
			outline.add(line);
			add(line);
		}
		add(new PointerArea(0, mapTop, WIDTH, WIDTH) {
			@Override
			public void onClick(PointerEvent event) {
				PointF point = camera().screenToCamera((int) event.current.x, (int) event.current.y);
				choose(map.cellAt(point.x, point.y, 0, mapTop, WIDTH));
			}
		});
		pos = mapTop + WIDTH + 2 * GAP;
		float half = (WIDTH - GAP) / 2f;
		teleport = addButton("Teleport", 0, half, () -> {
			hide();
			Teleport.toDepth(depth, branch, chosen);
		});
		teleport.enable(false);
		addButton("Back", half + GAP, half, () -> {
			hide();
			Teleport.askArrival(title, depth);
		});
		pos += ROW_HEIGHT;
		finish();
	}

	/**
	 * Outlines [cell] as the tile to teleport to, when it is one the hero explored.
	 */
	private void choose(int cell) {
		if (!map.knows(cell)) return;
		chosen = cell;
		float[] box = map.cellBox(cell, 0, mapTop, WIDTH);
		float left = box[0] - OUTLINE;
		float top = box[1] - OUTLINE;
		float size = box[2] + 2 * OUTLINE;
		place(outline.get(0), left, top, size, OUTLINE);
		place(outline.get(1), left, top + size - OUTLINE, size, OUTLINE);
		place(outline.get(2), left, top, OUTLINE, size);
		place(outline.get(3), left + size - OUTLINE, top, OUTLINE, size);
		teleport.enable(true);
	}

	private static void place(ColorBlock line, float x, float y, float width, float height) {
		line.size(width, height);
		line.x = x;
		line.y = y;
		line.visible = true;
	}

	@Override
	protected void reopen() {
		FloorMapWindow window = new FloorMapWindow(title, depth, branch, map);
		show(window);
		if (chosen >= 0) window.choose(chosen);
	}
}
