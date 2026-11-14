package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.io.File;
import java.util.List;

/**
 * Picks a save of a finished or damaged game to put back into its slot, so the game can be continued from it.
 */
public class RestoreSaveWindow extends EditorWindow {

	private static final String INFO = "Restore a Save"
		+ "\n\nPut one of this game's saves back into its slot, then continue the game from the title screen as usual. The other saves are kept."
		+ "\n\nIf the slot still holds a damaged game, a copy of it is moved to the app's cache first.";
	private static final int PER_PAGE = 6;

	private final File folder;
	private final int slot;
	private final int page;

	public RestoreSaveWindow(File folder, int slot, int page) {
		super();
		this.folder = folder;
		this.slot = slot;
		this.page = page;
		addTitle("Restore to slot " + slot, INFO);
		List<GameSaves.Save> saves = GameSaves.savesOf(folder);
		if (saves.isEmpty()) addHint("This game has no saves.");
		addPagedList(saves.size(), PER_PAGE, page, index -> {
			GameSaves.Save save = saves.get(index);
			addListRow(save.title(), save.locked ? new ItemSprite(ItemSpriteSheet.IRON_KEY) : null, false, true, () -> confirm(save), null);
		}, newPage -> replaceWith(new RestoreSaveWindow(folder, slot, newPage)));
		addButton("Back", 0, WIDTH, () -> {
			hide();
			GameSaves.askAgain(folder);
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private void confirm(GameSaves.Save save) {
		hide();
		show(new RestorePreviewWindow(save.title(), save.preview(), save.hero(), () -> {
			String error = GameSaves.restoreToSlot(save, folder, slot);
			if (error != null) GLog.w(error);
		}, this::reopen));
	}

	@Override
	public void onBackPressed() {
		hide();
		GameSaves.askAgain(folder);
	}

	@Override
	protected void reopen() {
		show(new RestoreSaveWindow(folder, slot, page));
	}
}
