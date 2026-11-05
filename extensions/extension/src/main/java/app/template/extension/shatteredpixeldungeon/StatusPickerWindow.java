package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
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
	private static Char target;
	private static Runnable backAction;

	/**
	 * Opens the picker for [character]. [back] reopens the window the picker was opened from.
	 */
	static void openFor(Char character, Runnable back) {
		target = character;
		backAction = back;
		show(new StatusPickerWindow(FAVORITES, 0));
	}

	private final int kind;
	private final int page;

	public StatusPickerWindow(int kind, int page) {
		super();
		this.kind = kind;
		this.page = page;
		Char hero = target != null ? target : Dungeon.hero;
		boolean isHero = hero == Dungeon.hero;

		addTitle("Add status", "Tap a status to add it for the chosen duration. Infinite makes it never run out.\n\nYellow statuses are already active, and adding them again extends them.\n\n{HEART}The heart adds a status to Favorites.\n\n{INFO}Tap i or long-press to read one.");
		addButtonRow(KIND_NAMES, kind, index -> {
			if (index != kind) replaceWith(new StatusPickerWindow(index, 0));
		});
		addStepper("Duration", duration + " turns", duration, 5, 1, 9999, value -> duration = Math.max(1, Math.min(9999, value)));
		if (isHero) addCheckBox("Infinite", infinite, () -> infinite = !infinite);
		pos += 2 * GAP;

		List<StatusEditor.Entry> entries = StatusEditor.catalog(kind);
		if (entries.isEmpty()) addHint("No favorites yet. Tap the heart next to a status to add it here.");
		addPagedList(entries.size(), perPage(7, 4), page, index -> {
			StatusEditor.Entry entry = entries.get(index);
			addListRow(entry.name, statusIcon(entry), StatusEditor.isActive(hero, entry.type), true, () -> {
				StatusEditor.add(hero, entry.type, duration);
				if (infinite && isHero) InfiniteStatuses.pin(entry.type, duration);
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
		Runnable action = backAction;
		if (action != null) {
			action.run();
		} else {
			CharacterWindow.open(CharacterWindow.TAB_STATUS, 1);
		}
	}

	@Override
	protected void reopen() {
		show(new StatusPickerWindow(kind, page));
	}
}
