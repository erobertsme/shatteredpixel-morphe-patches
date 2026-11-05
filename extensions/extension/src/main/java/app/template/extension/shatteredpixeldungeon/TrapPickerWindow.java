package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;

import java.util.ArrayList;
import java.util.List;

public class TrapPickerWindow extends EditorWindow {

	private static final String[] KINDS = { "Favorites", "All traps" };
	private static final String INFO = "Tap a trap to place it on this tile, replacing any trap already there. Hidden places it hidden, the way the dungeon does.\n\nThe heart adds a trap to Favorites. Tap i or long-press to read one.";

	private static boolean hidden;

	private final int cell;
	private final int kind;
	private final int page;
	private final Trap replacing;

	public TrapPickerWindow(int cell, int kind, int page, Trap replacing) {
		super();
		this.cell = cell;
		this.kind = kind;
		this.page = page;
		this.replacing = replacing;

		addTitle(replacing == null ? "Place trap" : "Change trap", INFO);
		addButtonRow(KINDS, kind, index -> {
			if (index != kind) replaceWith(new TrapPickerWindow(cell, index, 0, replacing));
		});
		addCheckBox("Hidden", hidden, () -> hidden = !hidden);
		pos += GAP;

		List<String[]> traps = traps(kind);
		if (traps.isEmpty()) addHint("No favorites yet. Tap the heart next to a trap to add it here.");
		addPagedList(traps.size(), perPage(7, 3), page, index -> {
			String className = traps.get(index)[0];
			String name = traps.get(index)[1];
			addListRow(name, null, replacing != null && replacing.getClass().getName().equals(className), true, () -> {
				FloorEditor.placeTrap(className, cell, hidden);
				back();
			}, () -> showInfo(className, name), new Favorite() {
				@Override
				public boolean isFavorite() {
					return Favorites.isTrap(className);
				}

				@Override
				public void toggle() {
					Favorites.toggleTrap(className);
				}
			});
		}, newPage -> replaceWith(new TrapPickerWindow(cell, kind, newPage, replacing)));

		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private static List<String[]> traps(int kind) {
		List<String[]> result = new ArrayList<>();
		for (String className : FloorEditor.TRAPS) {
			if (kind == 0 && !Favorites.isTrap(className)) continue;
			Trap sample = sample(className);
			if (sample == null) continue;
			result.add(new String[] { className, FloorEditor.trapName(sample) });
		}
		result.sort((a, b) -> a[1].compareToIgnoreCase(b[1]));
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Trap sample(String className) {
		try {
			return ((Class<? extends Trap>) Class.forName(className)).getConstructor().newInstance();
		} catch (Throwable e) {
			return null;
		}
	}

	private static void showInfo(String className, String name) {
		Trap sample = sample(className);
		if (sample == null) return;
		try {
			show(new WndMessage(name + "\n\n" + sample.desc()));
		} catch (Throwable ignored) {
		}
	}

	private void back() {
		hide();
		TileWindow.open(cell);
	}

	@Override
	protected void reopen() {
		show(new TrapPickerWindow(cell, kind, page, replacing));
	}
}
