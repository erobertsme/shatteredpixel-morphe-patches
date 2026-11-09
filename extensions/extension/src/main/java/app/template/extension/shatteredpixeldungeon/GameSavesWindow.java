package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.List;

public class GameSavesWindow extends EditorWindow {

	private static final String INFO = "Game Saves"
		+ "\n\nKeep up to 10 saves of the game you're playing, and load any of them later. Saves belong to this game only: another game can't see or load them."
		+ "\n\nSave Now saves the game and keeps a copy. Tap a save to see a picture of the dungeon when it was made, the last game messages, and buttons to load or delete it. Loading replaces where you are now, so save first if you want to come back to it."
		+ "\n\nSaves are kept in the save folder, in a folder for each game named after its slot followed by its seed, so the first slot's saves start with game1. They are included when you export saves from the main menu."
		+ "\n\nWhen this game ends, the title screen asks whether to delete or keep its saves.";
	private static final int PER_PAGE = 5;

	private final int page;
	private final boolean fromGameMenu;

	public GameSavesWindow(int page) {
		this(page, false);
	}

	public GameSavesWindow(int page, boolean fromGameMenu) {
		super();
		this.page = page;
		this.fromGameMenu = fromGameMenu;
		addTitle("Game Saves", INFO);

		List<GameSaves.Save> saves = GameSaves.list();
		if (saves.isEmpty()) addHint("No saves for this game yet.");
		addPagedList(saves.size(), PER_PAGE, page, index -> {
			GameSaves.Save save = saves.get(index);
			addListRow(save.title(), null, false, true, () -> choose(save), null);
		}, newPage -> replaceWith(new GameSavesWindow(newPage, fromGameMenu)));

		addButton("Save Now (" + saves.size() + "/" + GameSaves.MAX_SAVES + ")", 0, WIDTH, () -> {
			String error = GameSaves.create();
			if (error != null) GLog.w(error);
			refresh();
		}).enable(saves.size() < GameSaves.MAX_SAVES);
		pos += ROW_HEIGHT + 2 * GAP;

		addButton("Back", 0, WIDTH, () -> {
			hide();
			if (fromGameMenu) {
				CloverlordMenu.showGameMenu();
			} else {
				CloverlordMenu.show();
			}
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private void choose(GameSaves.Save save) {
		hide();
		show(new SavePreviewWindow(save, page, fromGameMenu));
	}

	@Override
	protected void reopen() {
		show(new GameSavesWindow(page, fromGameMenu));
	}
}
