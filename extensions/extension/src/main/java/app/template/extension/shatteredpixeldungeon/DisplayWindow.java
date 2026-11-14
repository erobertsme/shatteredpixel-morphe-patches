package app.template.extension.shatteredpixeldungeon;

/**
 * Display settings: how many pixels to keep clear at each screen edge.
 */
public class DisplayWindow extends EditorWindow {

	private static final String INFO = "Display"
		+ "\n\nShrink the game inward from each edge of the screen, in screen pixels. Use it when the game is cut off by a rounded corner, or something like the camera hole or a gesture bar covers part of it."
		+ "\n\nThe margins are painted black, and the game's buttons and windows move inward the same way they do for a notch. The screen is laid out again each time you change a margin."
		+ "\n\nTags editor shows every tag on the screen at once, the game's own Attack, Pick Up, Action and Resume tags included. Tap a tag for all of its settings, or press and hold it briefly and then drag it along its edge or onto another edge; changes apply straight away. Reset puts them all back."
		+ "\n\nTags always stay inside the margins.\n\nThese settings apply to every game.";

	public DisplayWindow() {
		super();
		addTitle("Display", INFO);
		for (DisplayMargins.Edge edge : DisplayMargins.Edge.values()) {
			int value = DisplayMargins.get(edge);
			addStepper(edge.label, value + " px", value, DisplayMargins.STEP, 0, DisplayMargins.MAX, pixels -> DisplayMargins.set(edge, pixels), true, () -> DisplayMargins.set(edge, 0));
		}
		pos += 2 * GAP;
		addButton("Tags editor", 0, WIDTH, () -> {
			hide();
			TagLayoutEditor.open(() -> show(new DisplayWindow()));
		});
		pos += ROW_HEIGHT + GAP;
		addButton("Reset all", 0, WIDTH, () -> {
			DisplayMargins.reset();
			refresh();
		});
		pos += ROW_HEIGHT + GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new DisplayWindow());
	}
}
