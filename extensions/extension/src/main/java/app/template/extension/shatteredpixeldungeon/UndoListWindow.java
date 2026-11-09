package app.template.extension.shatteredpixeldungeon;

/**
 * Lists every undo step kept for this game, newest first, so you can jump straight to one.
 */
public class UndoListWindow extends EditorWindow {

	private static final String INFO = "Undo History"
		+ "\n\nEvery step kept for this game, newest first. The step you are on now is marked (now) and the step you have picked is highlighted."
		+ "\n\nEach row shows the step's depth (D), turn (T) and the first game message from that action.\n\nTap a step to pick it and see what the dungeon looked like. Tap Done in undo mode to load it. Steps after the one you load stay in the list until you take a new action, so you can still return to them.";
	private final int page;

	public UndoListWindow() {
		this(pageOf(UndoHistory.selected()));
	}

	public UndoListWindow(int page) {
		super();
		this.page = page;
		addTitle("Undo History", INFO);

		int count = UndoHistory.count();
		if (count == 0) addHint("No undo steps yet.");
		addPagedList(count, perPage(), page, row -> {
			int position = count - 1 - row;
			boolean current = position == UndoHistory.current();
			String label = UndoHistory.listLabel(position) + (current ? " (now)" : "");
			addListRow(label, null, position == UndoHistory.selected(), UndoHistory.canSelect(position), () -> {
				hide();
				UndoHistory.select(position);
			}, null);
		}, newPage -> replaceWith(new UndoListWindow(newPage)));

		addButton("Close", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private static int pageOf(int position) {
		if (position < 0) return 0;
		return (UndoHistory.count() - 1 - position) / perPage();
	}

	private static int perPage() {
		return perPage(8, 4);
	}

	@Override
	protected void reopen() {
		show(new UndoListWindow(page));
	}
}
