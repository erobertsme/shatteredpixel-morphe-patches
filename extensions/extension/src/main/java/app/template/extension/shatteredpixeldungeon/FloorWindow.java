package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

import java.util.List;

public class FloorWindow extends EditorWindow {

	private static final String[] TABS = { "Effects", "Traps", "Other" };
	private static final String[] INFO = {
		"Effects\n\nEvery gas and area effect on this floor with its total strength. Remove clears one completely, Remove all clears them all.\n\nTo edit a single tile, examine it with the magnifying glass and tap Edit tile.",
		"Traps\n\nReveal and Hide change whether you can see the traps on this floor. Disarm stops them going off, Re-arm switches them back on. Remove all traps takes every trap off this floor.",
		"Other\n\nReveal map maps the whole floor and uncovers every secret door and hidden trap, like a Scroll of Magic Mapping. Forget map clears what you have explored.\n\nDraw: pick Floor, Wall or Eraser, then drag across the map to paint tiles. You can switch brushes with the buttons at the bottom. Move view lets you drag the map around to reach other areas, then pick a brush again to keep drawing. The eraser removes everything on a tile and leaves an empty pit. Tiles with a creature on them are skipped for walls and the eraser.\n\nFinish (green, bottom right) keeps your changes. Cancel (red, bottom left) puts the floor back exactly as it was before you started drawing.\n\nCreatures: defeat every enemy, ally or neutral creature on this floor at once.",
	};

	private static Runnable backAction;
	private static int tab;

	/**
	 * Replaced by the Floor editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	static void open(Runnable back) {
		backAction = back;
		tab = 0;
		show(new FloorWindow());
	}

	public FloorWindow() {
		super();
		addTitle("FLOOR " + Dungeon.depth, INFO[tab]);
		addButtonRow(TABS, tab, index -> {
			tab = index;
			refresh();
		});
		pos += GAP;

		float start = pos;
		float height = 0;
		for (int i = 0; i < TABS.length; i++) {
			if (i == tab) continue;
			int index = i;
			height = Math.max(height, measure(() -> buildTab(index)));
		}
		buildTab(tab);
		pos = Math.max(pos, start + height);

		pos += GAP;
		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private void buildTab(int index) {
		if (index == 0) buildEffects();
		if (index == 1) buildTraps();
		if (index == 2) buildOther();
	}

	private void buildEffects() {
		List<Blob> effects = FloorEditor.activeEffects();
		if (effects.isEmpty()) addHint("No effects on this floor.");
		for (Blob blob : effects) addEffectRow(blob);
		addButton("Remove all effects", 0, WIDTH, () -> {
			FloorEditor.clearAllEffects();
			refresh();
		}).enable(!effects.isEmpty());
		pos += ROW_HEIGHT + GAP;
	}

	private void buildTraps() {
		List<Trap> traps = FloorEditor.traps();
		int hidden = 0;
		int disarmed = 0;
		for (Trap trap : traps) {
			if (!trap.visible) hidden++;
			if (!trap.active) disarmed++;
		}
		addHint(traps.size() + " traps: " + hidden + " hidden, " + disarmed + " disarmed.");
		float half = (WIDTH - GAP) / 2f;
		addButton("Reveal all", 0, half, () -> run(FloorEditor::revealAll)).enable(hidden > 0);
		addButton("Hide all", half + GAP, half, () -> run(FloorEditor::hideAll)).enable(!traps.isEmpty());
		pos += ROW_HEIGHT + GAP;
		addButton("Disarm all", 0, half, () -> run(FloorEditor::disarmAll)).enable(disarmed < traps.size());
		addButton("Re-arm all", half + GAP, half, () -> run(FloorEditor::rearmAll)).enable(disarmed > 0);
		pos += ROW_HEIGHT + GAP;
		RedButton removeAll = addButton("Remove all traps", 0, WIDTH, () -> run(FloorEditor::removeAllTraps));
		removeAll.icon(Icons.get(Icons.CLOSE));
		removeAll.enable(!traps.isEmpty());
		pos += ROW_HEIGHT + GAP;
	}

	private void buildOther() {
		addSection("Map");
		float half = (WIDTH - GAP) / 2f;
		addButton("Reveal map", 0, half, () -> run(FloorEditor::revealMap));
		addButton("Forget map", half + GAP, half, () -> run(FloorEditor::forgetMap));
		pos += ROW_HEIGHT + GAP;

		addSection("Draw");
		float third = (WIDTH - 2 * GAP) / 3f;
		addButton("Floor", 0, third, () -> draw(DrawingSession.FLOOR));
		addButton("Wall", third + GAP, third, () -> draw(DrawingSession.WALL));
		addButton("Eraser", 2 * (third + GAP), third, () -> draw(DrawingSession.ERASER)).icon(Icons.get(Icons.CLOSE));
		pos += ROW_HEIGHT + GAP;

		addSection("Creatures");
		addGroupButton("Defeat all enemies", Char.Alignment.ENEMY);
		addGroupButton("Defeat all allies", Char.Alignment.ALLY);
		addGroupButton("Defeat all neutral", Char.Alignment.NEUTRAL);
	}

	private void draw(int brush) {
		hide();
		DrawingSession.start(brush);
	}

	private void addGroupButton(String label, Char.Alignment alignment) {
		int count = FloorEditor.countCreatures(alignment);
		RedButton button = addButton(label + " (" + count + ")", 0, WIDTH, () -> run(() -> FloorEditor.defeatCreatures(alignment)));
		button.icon(Icons.get(Icons.CLOSE));
		button.enable(count > 0);
		pos += ROW_HEIGHT + GAP;
	}

	private void addEffectRow(Blob blob) {
		float small = 34;
		float nameWidth = WIDTH - small - GAP;
		RedButton name = new RedButton("") {
			@Override
			public void onClick() {
				TileWindow.showEffectInfo(blob);
			}
		};
		name.setRect(0, pos, nameWidth, ROW_HEIGHT);
		add(name);
		RenderedTextBlock label = PixelScene.renderTextBlock(FloorEditor.effectName(blob.getClass()) + " (" + blob.volume + " total)", 6);
		label.maxWidth((int) nameWidth - 6);
		label.setPos(3, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		addButton("Remove", nameWidth + GAP, small, () -> {
			FloorEditor.clearAll(blob);
			refresh();
		}).icon(Icons.get(Icons.CLOSE));
		pos += ROW_HEIGHT + GAP;
	}

	private void run(Runnable action) {
		action.run();
		refresh();
	}

	private void back() {
		hide();
		Runnable action = backAction;
		backAction = null;
		if (action != null) action.run();
	}

	@Override
	protected void reopen() {
		show(new FloorWindow());
	}
}
