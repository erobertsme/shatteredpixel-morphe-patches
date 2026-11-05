package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

import java.util.List;

public class TileWindow extends EditorWindow {

	private static final String INFO = "Tile editor\n\nEffects lists every gas or area effect on this tile. Here clears it from this tile only. All removes it from the whole floor. Add effect places a new one on this tile.\n\nTrap: Active means it goes off when stepped on. Visible means you can see it. Trigger sets it off now. Change swaps it for another trap type. Remove takes it away. Without a trap, Add trap places one here.\n\nDoor: Lock turns it into a locked door that needs a key, Unlock turns any locked or crystal door into a normal door. Hide turns it into a hidden door you have to search for.\n\nFloor opens the editor for the whole floor.";

	private static Runnable backAction;
	private final int cell;

	static void open(int cell, Runnable back) {
		backAction = back;
		show(new TileWindow(cell));
	}

	static void open(int cell) {
		show(new TileWindow(cell));
	}

	public TileWindow(int cell) {
		super();
		this.cell = cell;
		int width = Dungeon.level.width();
		addTitle("TILE " + (cell % width) + ", " + (cell / width), INFO);

		addSection("Effects on this tile");
		boolean any = false;
		for (Blob blob : FloorEditor.activeEffects()) {
			int here = FloorEditor.strengthAt(blob, cell);
			if (here <= 0) continue;
			any = true;
			addEffectRow(blob, here);
		}
		if (!any) addHint("None.");
		addButton("Add effect", 0, WIDTH, () -> replaceWith(new EffectPickerWindow(cell, 0, 0)));
		pos += ROW_HEIGHT + GAP;

		if (FloorEditor.isDoor(cell)) addDoor();

		addSection("Trap");
		Trap trap = FloorEditor.trapAt(cell);
		if (trap == null) {
			addButton("Add trap", 0, WIDTH, () -> replaceWith(new TrapPickerWindow(cell, 0, 0, null))).enable(FloorEditor.canPlaceTrap(cell));
			pos += ROW_HEIGHT + GAP;
		} else {
			addTrap(trap);
		}

		pos += GAP;
		float half = (WIDTH - GAP) / 2f;
		addButton("Floor", 0, half, () -> {
			hide();
			FloorWindow.open(() -> open(cell));
		});
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private void addEffectRow(Blob blob, int here) {
		float small = 26;
		float nameWidth = WIDTH - 2 * (small + GAP);
		RedButton name = new RedButton("") {
			@Override
			public void onClick() {
				showEffectInfo(blob);
			}
		};
		name.setRect(0, pos, nameWidth, ROW_HEIGHT);
		add(name);
		RenderedTextBlock label = PixelScene.renderTextBlock(FloorEditor.effectName(blob.getClass()) + " (" + here + " here, " + blob.volume + " floor)", 6);
		label.maxWidth((int) nameWidth - 6);
		label.setPos(3, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);

		addButton("Here", nameWidth + GAP, small, () -> {
			FloorEditor.clearHere(blob, cell);
			refresh();
		});
		addButton("All", nameWidth + small + 2 * GAP, small, () -> {
			FloorEditor.clearAll(blob);
			refresh();
		}).textColor(CharacterWindow.NEGATIVE_COLOR);
		pos += ROW_HEIGHT + GAP;
	}

	private void addDoor() {
		int tile = FloorEditor.tileAt(cell);
		addSection(FloorEditor.doorState(cell));
		boolean locked = tile == FloorEditor.LOCKED_DOOR || tile == FloorEditor.CRYSTAL_DOOR || tile == FloorEditor.HERO_LOCKED_DOOR;
		boolean hidden = tile == FloorEditor.SECRET_DOOR;
		float half = (WIDTH - GAP) / 2f;
		addButton(locked ? "Unlock" : "Lock", 0, half, () -> {
			FloorEditor.setDoor(cell, locked ? FloorEditor.DOOR : FloorEditor.LOCKED_DOOR);
			refresh();
		});
		addButton(hidden ? "Reveal" : "Hide", half + GAP, half, () -> {
			FloorEditor.setDoor(cell, hidden ? FloorEditor.DOOR : FloorEditor.SECRET_DOOR);
			refresh();
		});
		pos += ROW_HEIGHT + GAP;
	}

	private void addTrap(Trap trap) {
		Image icon = null;
		try {
			icon = TerrainFeaturesTilemap.tile(cell, Dungeon.level.map[cell]);
		} catch (Throwable ignored) {
		}
		addListRow(FloorEditor.trapName(trap) + " (" + FloorEditor.trapState(trap) + ")", icon, false, true, () -> showTrapInfo(trap), null);
		addCheckBox("Active", trap.active, () -> FloorEditor.setActive(trap, !trap.active));
		addCheckBox("Visible", trap.visible, () -> FloorEditor.setVisible(trap, !trap.visible));

		float third = (WIDTH - 2 * GAP) / 3f;
		addButton("Trigger", 0, third, () -> {
			hide();
			FloorEditor.trigger(trap);
		});
		addButton("Change", third + GAP, third, () -> replaceWith(new TrapPickerWindow(cell, 0, 0, trap)));
		addButton("Remove", 2 * (third + GAP), third, () -> {
			FloorEditor.removeTrap(trap);
			refresh();
		}).textColor(CharacterWindow.NEGATIVE_COLOR);
		pos += ROW_HEIGHT + GAP;
	}

	static void showEffectInfo(Blob blob) {
		try {
			String desc = blob.tileDesc();
			show(new WndMessage(FloorEditor.effectName(blob.getClass()) + (desc == null ? "" : "\n\n" + desc)));
		} catch (Throwable ignored) {
		}
	}

	private static void showTrapInfo(Trap trap) {
		try {
			show(new WndMessage(FloorEditor.trapName(trap) + "\n\n" + trap.desc()));
		} catch (Throwable ignored) {
		}
	}

	private void back() {
		hide();
		Runnable action = backAction;
		backAction = null;
		if (action != null) action.run();
	}

	@Override
	protected void reopen() {
		show(new TileWindow(cell));
	}
}
