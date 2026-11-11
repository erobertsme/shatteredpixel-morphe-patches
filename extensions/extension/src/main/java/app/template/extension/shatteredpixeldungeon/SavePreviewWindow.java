package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * A game save in the Game Saves list, with buttons to load, lock, delete or go back.
 */
public class SavePreviewWindow extends PreviewWindow {

	private final GameSaves.Save save;
	private final int page;
	private final boolean fromGameMenu;

	public SavePreviewWindow(GameSaves.Save save, int page, boolean fromGameMenu) {
		super();
		this.save = save;
		this.page = page;
		this.fromGameMenu = fromGameMenu;
		layout(save.title(), save.preview(), save.hero());
	}

	@Override
	protected void addActions() {
		addButtonPair("Load", this::load, save.locked ? "Unlock" : "Lock", this::toggleLock);
		float half = (WIDTH - GAP) / 2f;
		addButton("Delete", 0, half, this::delete).enable(!save.locked);
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT;
	}

	private void load() {
		hide();
		String error = GameSaves.load(save);
		if (error != null) GLog.w(error);
	}

	private void toggleLock() {
		String error = GameSaves.setLocked(save, !save.locked);
		if (error != null) {
			GLog.w(error);
			return;
		}
		GameSaves.Save updated = save;
		for (GameSaves.Save candidate : GameSaves.list()) {
			if (candidate.folder.equals(save.folder)) updated = candidate;
		}
		hide();
		show(new SavePreviewWindow(updated, page, fromGameMenu));
	}

	private void delete() {
		if (save.locked) return;
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
