package app.template.extension.shatteredpixeldungeon;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks what a new quick tag does: open a window, or run an action. Only what your patches include is listed.
 */
public class QuickActionPickerWindow extends EditorWindow {

	private static final String[] KINDS = { "Windows", "Actions" };

	private final int kind;
	private final int page;

	public QuickActionPickerWindow(int kind) {
		this(kind, 0);
	}

	public QuickActionPickerWindow(int kind, int page) {
		super();
		this.kind = kind;
		this.page = page;
		addTitle("Add Quick Tag");
		addButtonRow(KINDS, kind, index -> {
			if (index != kind) replaceWith(new QuickActionPickerWindow(index));
		});

		List<QuickActions.Action> choices = choices(kind == 0);
		if (choices.isEmpty()) addHint("Nothing here. The patches or editors these need aren't included or are switched off.");
		addPagedList(choices.size(), perPage(8, 4), page, index -> {
			QuickActions.Action action = choices.get(index);
			addListRow(action.name, IconCatalog.image(action.icon), false, true, () -> {
				hide();
				QuickTagsWindow.openTag(QuickTags.add(action));
			}, null);
		}, newPage -> replaceWith(new QuickActionPickerWindow(kind, newPage)));

		addButton("Back", 0, WIDTH, () -> {
			hide();
			show(new QuickTagsWindow());
		});
		pos += ROW_HEIGHT;
		finish();
	}

	/**
	 * The windows, or the actions, whose patch and editor are included and switched on. Checked the same way the tags check before running,
	 * except that being in a game isn't needed to make a tag.
	 */
	private static List<QuickActions.Action> choices(boolean windows) {
		List<QuickActions.Action> list = new ArrayList<>();
		for (QuickActions.Action action : QuickActions.all()) {
			if (action.opensWindow == windows && action.isIncluded()) list.add(action);
		}
		return list;
	}

	@Override
	protected void reopen() {
		show(new QuickActionPickerWindow(kind, page));
	}
}
