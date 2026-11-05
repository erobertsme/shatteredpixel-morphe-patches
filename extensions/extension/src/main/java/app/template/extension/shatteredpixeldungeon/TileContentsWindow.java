package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class TileContentsWindow extends EditorWindow {

	private static final String INFO = "On this tile\n\nEverything on the tile you examined. Tap a row to see its details.\n\nWhen several items lie in a pile, each one is listed. Pick one and use Pick Up to take that item instead of the one on top.\n\nThe last row is the tile itself, so you can still examine or edit a tile that has an item or a creature on it.";

	private final int cell;

	public TileContentsWindow(int cell) {
		super();
		this.cell = cell;
		addTitle("On this tile", INFO);
		for (Object object : TileContents.objectsAt(cell)) addObject(object);
		addTile();
		finish();
	}

	private void addObject(Object object) {
		if (object instanceof Heap) {
			addHeap((Heap) object);
			return;
		}
		if (object instanceof Hero) {
			addListRow("You (" + InventoryEditor.capitalize(((Hero) object).className()) + ")", null, false, true, () -> examine(object), null);
			return;
		}
		if (object instanceof Mob) {
			Mob mob = (Mob) object;
			addListRow(InventoryEditor.capitalize(mob.name()), mobIcon(mob), false, true, () -> examine(object), null);
			return;
		}
		if (object instanceof Plant) {
			addListRow(InventoryEditor.capitalize(((Plant) object).name()), featureIcon(), false, true, () -> examine(object), null);
			return;
		}
		if (object instanceof Trap) {
			addListRow(InventoryEditor.capitalize(((Trap) object).name()), featureIcon(), false, true, () -> examine(object), null);
		}
	}

	private void addHeap(Heap heap) {
		if (heap.type != Heap.Type.HEAP || heap.items == null) {
			addListRow(InventoryEditor.capitalize(heap.title()), heapIcon(heap), false, true, () -> examine(heap), null);
			return;
		}
		for (Item item : new ArrayList<>(heap.items)) {
			addListRow(InventoryEditor.capitalize(item.title()), new ItemSprite(item), false, true, () -> replaceWith(new PileItemWindow(cell, item)), null);
		}
	}

	private void addTile() {
		Image icon = null;
		try {
			icon = WndInfoCell.cellImage(cell);
		} catch (Throwable ignored) {
		}
		String name;
		try {
			name = WndInfoCell.cellName(cell);
		} catch (Throwable e) {
			name = "Tile";
		}
		addListRow("Tile: " + InventoryEditor.capitalize(name), icon, false, true, () -> replaceWith(new WndInfoCell(cell)), null);
	}

	private void examine(Object object) {
		hide();
		GameScene.examineObject(object);
	}

	private static Image mobIcon(Mob mob) {
		try {
			return mob.sprite();
		} catch (Throwable e) {
			return null;
		}
	}

	private static Image heapIcon(Heap heap) {
		try {
			return new ItemSprite(heap);
		} catch (Throwable e) {
			return null;
		}
	}

	private Image featureIcon() {
		try {
			return TerrainFeaturesTilemap.tile(cell, Dungeon.level.map[cell]);
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	protected void reopen() {
		show(new TileContentsWindow(cell));
	}
}
