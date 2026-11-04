package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;

import java.util.List;

public class StatusPickerWindow extends EditorWindow {

	static final int FAVORITES = 0;
	static final int POSITIVE = 1;
	static final int NEGATIVE = 2;
	static final int OTHER = 3;
	private static final String[] KIND_NAMES = { "Favs", "Positive", "Negative", "Other" };

	private static int duration = 20;
	private static boolean infinite;

	private final int kind;
	private final int page;

	public StatusPickerWindow(int kind, int page) {
		super();
		this.kind = kind;
		this.page = page;
		Hero hero = Dungeon.hero;

		addTitle("Add status");
		addButtonRow(KIND_NAMES, kind, index -> {
			if (index != kind) replaceWith(new StatusPickerWindow(index, 0));
		});
		addStepper("Duration", duration + " turns", duration, 5, 1, 9999, value -> duration = Math.max(1, Math.min(9999, value)));
		addCheckBox("Infinite", infinite, () -> infinite = !infinite);
		pos += 2 * GAP;

		List<StatusEditor.Entry> entries = StatusEditor.catalog(kind);
		if (entries.isEmpty()) addHint("No favorites yet. Tap the heart next to a status to add it here.");
		addPagedList(entries.size(), perPage(7, 4), page, index -> {
			StatusEditor.Entry entry = entries.get(index);
			addListRow(entry.name, statusIcon(entry), StatusEditor.isActive(hero, entry.type), true, () -> {
				StatusEditor.add(hero, entry.type, duration);
				if (infinite) InfiniteStatuses.pin(entry.type, duration);
				back();
			}, () -> CharacterWindow.showBuffInfo(entry.sample), new Favorite() {
				@Override
				public boolean isFavorite() {
					return Favorites.isStatus(entry.type);
				}

				@Override
				public void toggle() {
					Favorites.toggleStatus(entry.type);
				}
			});
		}, newPage -> replaceWith(new StatusPickerWindow(kind, newPage)));

		addHint("Tap to add, i or long-press to read. The heart adds it to Favorites. Yellow = already active.");
		addButton("Back", 0, WIDTH, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private static BuffIcon statusIcon(StatusEditor.Entry entry) {
		try {
			return new BuffIcon(entry.sample, true);
		} catch (Throwable e) {
			return null;
		}
	}

	private void back() {
		hide();
		CharacterWindow.open(CharacterWindow.TAB_STATUS, 1);
	}

	@Override
	protected void reopen() {
		show(new StatusPickerWindow(kind, page));
	}
}
