package app.template.extension.shatteredpixeldungeon;

/**
 * The Undo / Redo page in Extras, QoL.
 */
public class UndoSettingsWindow extends EditorWindow {

	private static final String INFO = "Undo / Redo\n\nEach time your hero is ready for a new action, the game is saved and a copy is kept. Undo goes back one action and Redo goes forward again, each with a short loading screen. A walk of many steps counts as one action. Taking a new action after an undo clears Redo.\n\nRandom rolls are not saved, so repeating an action after an undo can turn out differently. Redo always restores exactly what happened.\n\nKeep: Last actions keeps the number you choose. This floor keeps every action since you arrived on the floor. Unlimited keeps everything until you leave the game.\n\nKeep history across floors: when off, history starts over each time you change floors.\n\nAllow during boss fights: when off, Undo and Redo can't be used while a boss has the floor locked.\n\nTag Position & Color: choose the screen edge and how far along it the Undo and Redo tags sit, and their colour.\n\nPerformance shows how long each copy takes. Save is the game's own saving, copy is keeping it in memory.";

	public UndoSettingsWindow() {
		super();
		addTitle("Undo / Redo", INFO);
		addCheckBox("Undo and Redo", UndoHistory.isOn(), () -> UndoHistory.setOn(!UndoHistory.isOn()));
		boolean on = UndoHistory.isOn();

		addSection("Keep");
		int mode = UndoHistory.mode();
		addButtonRow(UndoHistory.MODES, mode, index -> {
			UndoHistory.mode(index);
			refresh();
		});
		if (mode == UndoHistory.MODE_LAST) {
			int size = UndoHistory.size();
			addStepper("Actions", String.valueOf(size), size, 1, 1, UndoHistory.MAX_SIZE, UndoHistory::size, on, () -> UndoHistory.size(UndoHistory.DEFAULT_SIZE));
		}
		addCheckBox("Keep history across floors", UndoHistory.acrossFloors(), () -> UndoHistory.acrossFloors(!UndoHistory.acrossFloors()), on && mode != UndoHistory.MODE_FLOOR);
		addCheckBox("Allow during boss fights", UndoHistory.duringBosses(), () -> UndoHistory.duringBosses(!UndoHistory.duringBosses()), on);

		addSection("Tags");
		addButton("Tag Position & Color", 0, WIDTH, () -> replaceWith(new TagSettingsWindow(UndoTags.PLACEMENT, "Undo / Redo Tags", () -> show(new UndoSettingsWindow()))));
		pos += ROW_HEIGHT + GAP;

		addSection("Performance");
		addIconRow(null, "Last copy", UndoHistory.lastTiming());
		addIconRow(null, "Average", UndoHistory.averageTiming());
		addIconRow(null, "Slowest", UndoHistory.slowestTiming());
		addIconRow(null, "History", UndoHistory.memoryText());
		float half = (WIDTH - GAP) / 2f;
		addButton("Reset Timing", 0, half, () -> {
			UndoHistory.resetTiming();
			refresh();
		});
		addButton("Clear History", half + GAP, half, () -> {
			UndoHistory.clear();
			refresh();
		});
		pos += ROW_HEIGHT + GAP;

		pos += GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new UndoSettingsWindow());
	}
}
