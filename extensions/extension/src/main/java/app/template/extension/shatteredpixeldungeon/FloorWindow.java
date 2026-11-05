package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

import java.util.List;

public class FloorWindow extends EditorWindow {

	private static final String INFO = "Floor editor\n\nEffects lists every gas and area effect on this floor with its total strength. Remove clears one completely, Remove all clears them all.\n\nTraps: Reveal and Hide change whether you can see them. Disarm stops them going off, Re-arm switches them back on. Remove all traps takes every trap off this floor.\n\nTo edit a single tile, examine it with the magnifying glass and tap Edit tile.";

	private static Runnable backAction;

	/**
	 * Replaced by the Floor editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	static void open(Runnable back) {
		backAction = back;
		show(new FloorWindow());
	}

	public FloorWindow() {
		super();
		addTitle("FLOOR " + Dungeon.depth, INFO);

		addSection("Effects on this floor");
		List<Blob> effects = FloorEditor.activeEffects();
		if (effects.isEmpty()) addHint("None.");
		for (Blob blob : effects) addEffectRow(blob);
		addButton("Remove all effects", 0, WIDTH, () -> {
			FloorEditor.clearAllEffects();
			refresh();
		}).enable(!effects.isEmpty());
		pos += ROW_HEIGHT + GAP;

		List<Trap> traps = FloorEditor.traps();
		int hidden = 0;
		int disarmed = 0;
		for (Trap trap : traps) {
			if (!trap.visible) hidden++;
			if (!trap.active) disarmed++;
		}
		addSection("Traps (" + traps.size() + ": " + hidden + " hidden, " + disarmed + " disarmed)");
		float half = (WIDTH - GAP) / 2f;
		addButton("Reveal all", 0, half, () -> run(FloorEditor::revealAll)).enable(hidden > 0);
		addButton("Hide all", half + GAP, half, () -> run(FloorEditor::hideAll)).enable(!traps.isEmpty());
		pos += ROW_HEIGHT + GAP;
		addButton("Disarm all", 0, half, () -> run(FloorEditor::disarmAll)).enable(disarmed < traps.size());
		addButton("Re-arm all", half + GAP, half, () -> run(FloorEditor::rearmAll)).enable(disarmed > 0);
		pos += ROW_HEIGHT + GAP;
		RedButton removeAll = addButton("Remove all traps", 0, WIDTH, () -> run(FloorEditor::removeAllTraps));
		removeAll.textColor(CharacterWindow.NEGATIVE_COLOR);
		removeAll.enable(!traps.isEmpty());
		pos += ROW_HEIGHT + 2 * GAP;

		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
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
		}).textColor(CharacterWindow.NEGATIVE_COLOR);
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
