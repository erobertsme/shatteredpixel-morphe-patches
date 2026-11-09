package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

import java.util.List;

public class FloorWindow extends EditorWindow {

	private static final String[] TABS = { "Effects", "Traps", "Other" };
	private static final String[] INFO = {
		"Effects\n\nEvery gas and area effect on this floor with its total strength. A Lotus from a Wand of Regrowth shows its range and the turns it has left: it covers every tile in range with falling leaves and helps plants drop seeds and dew there. Remove clears one completely, Remove All Effects clears them all.\n\nTo edit a single tile, examine it with the magnifying glass and tap Edit Tile.\n\nEdit Floor (the button above the tabs): the game is saved, then every tile you tap shows its info with an Edit button, and buttons for each creature, item, plant or trap on it. Drag to look around. Save keeps your changes. Cancel asks first, then puts the whole game back as it was when you started.",
		"Traps\n\nReveal and Hide change whether you can see the traps on this floor. Disarm All stops them going off, Re-arm All switches them back on. Remove All Traps takes every trap off this floor.\n\nEdit Floor (the button above the tabs): the game is saved, then every tile you tap shows its info with an Edit button, and buttons for each creature, item, plant or trap on it. Drag to look around. Save keeps your changes. Cancel asks first, then puts the whole game back as it was when you started.",
		"Other\n\nReveal Map maps the whole floor and uncovers every secret door and hidden trap, like a Scroll of Magic Mapping. Forget Map clears what you have explored.\n\nDraw: pick Floor, Wall or Eraser, then drag across the map to paint tiles. You can switch brushes with the buttons at the bottom. Move View lets you drag the map around to reach other areas, then pick a brush again to keep drawing. The eraser removes everything on a tile and leaves an empty pit. Tiles with a creature on them are skipped for walls and the eraser.\n\nFinish (green, bottom right) keeps your changes. Cancel (red, bottom left) puts the floor back exactly as it was before you started drawing.\n\nCreatures: defeat every enemy, ally or neutral creature on this floor at once.\n\nEdit Floor (the button above the tabs): the game is saved, then every tile you tap shows its info with an Edit button, and buttons for each creature, item, plant or trap on it. Drag to look around. Save keeps your changes. Cancel asks first, then puts the whole game back as it was when you started.",
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
		addButton("Edit Floor", 0, WIDTH, () -> {
			hide();
			FloorEditSession.start();
		});
		pos += ROW_HEIGHT + GAP;
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
		List<Mob> lotuses = FloorEditor.lotuses();
		if (effects.isEmpty() && lotuses.isEmpty()) addHint("No effects on this floor.");
		for (Blob blob : effects) addEffectRow(blob);
		for (Mob lotus : lotuses) addLotusRow(lotus);
		addButton("Remove all effects", 0, WIDTH, () -> {
			FloorEditor.clearAllEffects();
			refresh();
		}).enable(!effects.isEmpty() || !lotuses.isEmpty());
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
		float small = 16;
		float nameWidth = WIDTH - small - GAP;
		RedButton name = new RedButton("") {
			@Override
			public void onClick() {
				TileWindow.showEffectInfo(blob, -1);
			}
		};
		name.setRect(0, pos, nameWidth, ROW_HEIGHT);
		add(name);
		RenderedTextBlock label = PixelScene.renderTextBlock(FloorEditor.effectName(blob.getClass()) + " (" + blob.volume + " total)", 6);
		label.maxWidth((int) nameWidth - 6);
		label.setPos(3, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		IconButton remove = new IconButton(Icons.get(Icons.CLOSE)) {
			@Override
			public void onClick() {
				FloorEditor.clearAll(blob);
				refresh();
			}
		};
		remove.setRect(nameWidth + GAP, pos, small, ROW_HEIGHT);
		add(remove);
		pos += ROW_HEIGHT + GAP;
	}

	private void addLotusRow(Mob lotus) {
		float small = 16;
		float nameWidth = WIDTH - small - GAP;
		RedButton name = new RedButton("");
		name.setRect(0, pos, nameWidth, ROW_HEIGHT);
		add(name);
		RenderedTextBlock label = PixelScene.renderTextBlock("Lotus (range " + FloorEditor.lotusRange(lotus) + ", " + lotus.HP + " turns)", 6);
		label.maxWidth((int) nameWidth - 6);
		label.setPos(3, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		IconButton remove = new IconButton(Icons.get(Icons.CLOSE)) {
			@Override
			public void onClick() {
				EnemyEditor.defeat(lotus);
				refresh();
			}
		};
		remove.setRect(nameWidth + GAP, pos, small, ROW_HEIGHT);
		add(remove);
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
