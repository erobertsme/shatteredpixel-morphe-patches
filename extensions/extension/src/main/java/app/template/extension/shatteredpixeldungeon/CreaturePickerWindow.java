package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.Image;

import java.util.List;

public class CreaturePickerWindow extends EditorWindow {

	private static final String[] KINDS = { "Favs", "Enemies", "NPCs", "Other" };
	private static final String INFO = "Tap a creature to place it on this tile. Ally makes it fight for you.\n\nLevel: some creatures, such as mimics, bees and wraiths, grow stronger the deeper they appear. Those show their level in the list, and spawn at the level you set here. Auto uses the current floor. The reset button goes back to Auto.\n\nBosses and level-specific creatures can behave oddly away from their own floor.\n\nThe heart adds a creature to Favorites. Tap i or long-press to read one.";

	private static boolean ally;

	private final int cell;
	private final int kind;
	private final int page;

	public CreaturePickerWindow(int cell, int kind, int page) {
		super();
		this.cell = cell;
		this.kind = kind;
		this.page = page;

		addTitle("Spawn creature", INFO);
		addButtonRow(KINDS, kind, index -> {
			if (index != kind) replaceWith(new CreaturePickerWindow(cell, index, 0));
		});
		addCheckBox("Ally", ally, () -> ally = !ally);
		int level = FloorEditor.effectiveSpawnLevel();
		String display = FloorEditor.spawnLevel() == 0 ? "Auto " + level : String.valueOf(level);
		addStepper("Level", display, level, 1, 1, FloorEditor.MAX_SPAWN_LEVEL, value -> FloorEditor.spawnLevel(Math.max(1, value)), true, () -> FloorEditor.spawnLevel(0));
		pos += GAP;

		List<CreatureCatalog.Entry> entries = CreatureCatalog.catalog(kind);
		if (entries.isEmpty()) addHint("No favorites yet. Tap the heart next to a creature to add it here.");
		addPagedList(entries.size(), perPage(7, 3), page, index -> {
			CreatureCatalog.Entry entry = entries.get(index);
			String name = FloorEditor.scales(entry.type) ? entry.name + " (Lv " + level + ")" : entry.name;
			addListRow(name, sprite(entry), false, true, () -> {
				FloorEditor.spawn(entry.type, cell, ally);
				hide();
			}, () -> showInfo(entry), new Favorite() {
				@Override
				public boolean isFavorite() {
					return Favorites.isCreature(entry.type.getName());
				}

				@Override
				public void toggle() {
					Favorites.toggleCreature(entry.type.getName());
				}
			});
		}, newPage -> replaceWith(new CreaturePickerWindow(cell, kind, newPage)));

		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private static Image sprite(CreatureCatalog.Entry entry) {
		try {
			return entry.type.getConstructor().newInstance().sprite();
		} catch (Throwable e) {
			return null;
		}
	}

	private static void showInfo(CreatureCatalog.Entry entry) {
		try {
			Mob mob = entry.type.getConstructor().newInstance();
			show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(mob));
		} catch (Throwable ignored) {
		}
	}

	private void back() {
		hide();
		TileWindow.open(cell);
	}

	@Override
	protected void reopen() {
		show(new CreaturePickerWindow(cell, kind, page));
	}
}
