package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.List;

/**
 * Picks a kept undo step of a damaged game to put back into its slot, so the game can be continued from it.
 */
public class RestoreUndoWindow extends EditorWindow {

	private static final String INFO = "Restore an Undo Step"
		+ "\n\nPut one of this game's kept undo steps back into its slot, then continue the game from the title screen as usual. The newest step is"
		+ " first. Floors you weren't on at that step come from the game's own files in the slot."
		+ "\n\nWhatever is in the slot now is copied to the app's cache first.";
	private static final int PER_PAGE = 6;

	private final int slot;
	private final long seed;
	private final int page;

	public RestoreUndoWindow(int slot, long seed, int page) {
		super();
		this.slot = slot;
		this.seed = seed;
		this.page = page;
		addTitle("Restore to slot " + slot, INFO);
		List<UndoHistory.StoredStep> steps = UndoHistory.storedSteps(slot, seed);
		if (steps.isEmpty()) addHint("This game has no kept undo steps.");
		addPagedList(steps.size(), PER_PAGE, page, index -> {
			UndoHistory.StoredStep step = steps.get(index);
			addListRow(step.title(), null, index == 0, true, () -> confirm(step), null);
		}, newPage -> replaceWith(new RestoreUndoWindow(slot, seed, newPage)));
		addButton("Back", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private void confirm(UndoHistory.StoredStep step) {
		Object[] preview = UndoHistory.storedPreview(slot, seed, step.id);
		hide();
		show(new RestorePreviewWindow(step.title(), (UndoScene) preview[0], (Hero) preview[1], () -> {
			String error = UndoHistory.restoreStored(slot, seed, step.id);
			if (error != null) GLog.w(error);
		}, this::reopen));
	}

	@Override
	protected void reopen() {
		show(new RestoreUndoWindow(slot, seed, page));
	}
}
