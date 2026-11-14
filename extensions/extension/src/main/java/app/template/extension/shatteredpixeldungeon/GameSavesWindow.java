package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.List;

public class GameSavesWindow extends EditorWindow {

	private static final String INFO = "Game Saves"
		+ "\n\nKeep up to 10 saves of the game you're playing, and load any of them later. Saves belong to this game only: another game can't see or load them."
		+ "\n\nSave Now saves the game and keeps a copy. Once there are 10, saving again deletes the oldest unlocked save, after asking first. Locked saves, marked with a key, don't count towards the 10 and can't be deleted until you unlock them.\n\nAuto save on new floors: saves the game each time you arrive on a floor, including going into or coming out of a side area like the Blacksmith's mine. Auto saves start with \"Auto:\", keep the latest 5 (locked ones aside), and don't count towards your 10. Tap a save to see a picture of the dungeon when it was made, the last game messages, and buttons to load or delete it. Loading replaces where you are now, so save first if you want to come back to it."
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
			addListRow(save.title(), save.locked ? new ItemSprite(ItemSpriteSheet.IRON_KEY) : null, false, true, () -> choose(save), null);
		}, newPage -> replaceWith(new GameSavesWindow(newPage, fromGameMenu)));

		addCheckBox("Auto save on new floors", GameSaves.autoSaves(), () -> GameSaves.autoSaves(!GameSaves.autoSaves()));
		int counted = GameSaves.unlockedCount();
		addButton("Save Now (" + counted + "/" + GameSaves.MAX_SAVES + ")", 0, WIDTH, this::saveNow);
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

	private void saveNow() {
		GameSaves.Save oldest = GameSaves.unlockedCount() >= GameSaves.MAX_SAVES ? GameSaves.oldestUnlocked() : null;
		if (oldest == null) {
			save();
			return;
		}
		ConfirmPrompt.ask("Replace oldest save", "This game already has " + GameSaves.MAX_SAVES + " unlocked saves. Saving now deletes the oldest one:\n\n" + oldest.title()
			+ "\n\nLock a save to keep it.", "Save", this::save);
	}

	private void save() {
		String error = GameSaves.create();
		if (error != null) GLog.w(error);
		refresh();
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
