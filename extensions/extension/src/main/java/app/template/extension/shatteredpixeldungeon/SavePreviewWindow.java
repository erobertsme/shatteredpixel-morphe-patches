package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.PointerArea;

import java.util.ArrayList;
import java.util.List;

/**
 * A game save with a picture of the dungeon when it was made, the game messages from just before, and buttons to load or delete it.
 */
public class SavePreviewWindow extends EditorWindow {

	private static final int LOG_LINES = 4;
	private static final int LOG_LINE_HEIGHT = 8;
	private static final int LOG_PADDING = 2;
	private static final int LOG_BACK_COLOR = 0x66000000;
	private static final int BORDER = 1;

	private final GameSaves.Save save;
	private final int page;
	private final boolean fromGameMenu;

	public SavePreviewWindow(GameSaves.Save save, int page, boolean fromGameMenu) {
		super();
		this.save = save;
		this.page = page;
		this.fromGameMenu = fromGameMenu;
		addTitle(save.title());

		UndoScene scene = save.preview();
		if (scene == null || !scene.hasArea() && !scene.hasMap()) {
			addHint("No picture for this save. Saves made from now on keep one.");
		} else {
			addPicture(scene);
			addLog(scene.lines);
		}

		float half = (WIDTH - GAP) / 2f;
		addButton("Load", 0, half, this::load);
		addButton("Delete", half + GAP, half, this::delete);
		pos += ROW_HEIGHT + GAP;
		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private void addPicture(UndoScene scene) {
		boolean map = UndoTags.showsMap() ? scene.hasMap() : !scene.hasArea();
		float size = WIDTH - 2 * BORDER;
		float left = BORDER;
		float top = pos + BORDER;
		ColorBlock frame = new ColorBlock(size + 2 * BORDER, size + 2 * BORDER, 0xFF000000);
		frame.x = 0;
		frame.y = pos;
		add(frame);
		List<Gizmo> parts = new ArrayList<>();
		try {
			if (map) {
				scene.drawMap(parts, null, left, top, size);
			} else {
				scene.drawArea(parts, null, left, top, size);
			}
		} catch (Throwable e) {
			Log.e("SavePreviewWindow", "Could not draw the save", e);
		}
		for (Gizmo part : parts) add(part);
		if (scene.hasArea() && scene.hasMap()) {
			add(new PointerArea(left, top, size, size) {
				@Override
				public void onClick(PointerEvent event) {
					UndoTags.toggleMap();
					refresh();
				}
			});
			RenderedTextBlock hint = PixelScene.renderTextBlock(map ? "Tap for area" : "Tap for map", 6);
			hint.hardlight(0xCCCCCC);
			hint.setPos(left + size - hint.width() - 2, top + 2);
			PixelScene.align(hint);
			add(hint);
		}
		pos += size + 2 * BORDER + 2 * GAP;
	}

	private void addLog(List<String> lines) {
		float height = LOG_LINES * LOG_LINE_HEIGHT + 2 * LOG_PADDING;
		ColorBlock back = new ColorBlock(WIDTH, height, LOG_BACK_COLOR);
		back.x = 0;
		back.y = pos;
		add(back);
		if (lines.isEmpty()) {
			RenderedTextBlock none = PixelScene.renderTextBlock("No recent messages", 6);
			none.hardlight(CharacterWindow.DIM_COLOR);
			none.setPos(LOG_PADDING, pos + LOG_PADDING);
			PixelScene.align(none);
			add(none);
		}
		for (int k = 0; k < lines.size() && k < LOG_LINES; k++) {
			String line = lines.get(k);
			RenderedTextBlock block = PixelScene.renderTextBlock(UndoTags.fit(UndoScene.plain(line), 6, WIDTH - 2 * LOG_PADDING), 6);
			block.hardlight(UndoScene.lineColor(line));
			block.setPos(LOG_PADDING, pos + LOG_PADDING + k * LOG_LINE_HEIGHT);
			PixelScene.align(block);
			add(block);
		}
		pos += height + 2 * GAP;
	}

	private void load() {
		hide();
		String error = GameSaves.load(save);
		if (error != null) GLog.w(error);
	}

	private void delete() {
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
}
