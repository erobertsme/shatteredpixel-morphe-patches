package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

public class DepthPickerWindow extends EditorWindow {

	private static final String INFO = "Teleport to..."
		+ "\n\nPick any floor to go there straight away. You arrive at its entrance or its exit, your choice. The current floor is highlighted."
		+ "\n\nFloors you haven't reached yet are created when you arrive, just as if you had taken the stairs. Skipping floors also skips what's on them.";
	private static final String[] REGIONS = { "Sewers", "Prison", "Caves", "Dwarven City", "Demon Halls", "Amulet" };
	private static final int PER_ROW = 5;

	public DepthPickerWindow() {
		super();
		addTitle("Teleport to...", INFO);
		for (int region = 0; region < REGIONS.length; region++) {
			int first = region * PER_ROW + 1;
			if (first > Teleport.MAX_DEPTH) break;
			int last = Math.min(Teleport.MAX_DEPTH, first + PER_ROW - 1);
			addSection(REGIONS[region]);
			String[] labels = new String[PER_ROW];
			for (int i = 0; i < PER_ROW; i++) labels[i] = first + i <= last ? String.valueOf(first + i) : "";
			int current = Dungeon.depth - first;
			addButtonRow(labels, current >= 0 && current < PER_ROW ? current : -1, index -> choose(first + index));
		}
		pos += GAP;
		addButton("Close", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private void choose(int depth) {
		hide();
		ConfirmPrompt.ask("Depth " + depth, "Teleport to depth " + depth + "?", new String[] { "Arrive at Entrance", "Arrive at Exit" }, new Runnable[] {
			() -> Teleport.toDepth(depth, Teleport.ENTRANCE),
			() -> Teleport.toDepth(depth, Teleport.EXIT),
		});
	}

	@Override
	protected void reopen() {
		show(new DepthPickerWindow());
	}
}
