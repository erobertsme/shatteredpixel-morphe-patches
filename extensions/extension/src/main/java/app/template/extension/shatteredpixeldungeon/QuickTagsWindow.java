package app.template.extension.shatteredpixeldungeon;

import java.util.List;

/**
 * The Quick Tags page in Extras, QoL: the quick tags you've made, and Add Tag.
 */
public class QuickTagsWindow extends EditorWindow {

	private static final String INFO = "Quick Tags\n\nAdd Tag lets you pick an editor window, opened straight to one of its tabs where it has them, or an action like revealing the map, healing or defeating every enemy. A tag for it appears on the screen edge, and tapping the tag opens the window or runs the action. Each game keeps its own quick tags; the ones set on the main menu are the starting set for new games.\n\nTap a tag in the list to change its position, size, colour, icon and opacity, or to delete it. You can also move every tag at once with the Tags editor in Display.\n\nA tag whose editor is switched off, or that only works in a game, does nothing until it can run.";

	public QuickTagsWindow() {
		super();
		addTitle("Quick Tags", INFO);
		List<Integer> ids = QuickTags.ids();
		if (ids.isEmpty()) addHint("No quick tags yet.");
		for (int id : ids) {
			QuickActions.Action action = QuickTags.action(id);
			addListRow(QuickTags.name(id), IconCatalog.image(QuickTags.placement(id).icon()), false, action != null, () -> {
				hide();
				openTag(id);
			}, null);
		}
		pos += GAP;
		addButton("Add Tag", 0, WIDTH, () -> {
			hide();
			show(new QuickActionPickerWindow(0));
		});
		pos += ROW_HEIGHT + GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	static void openTag(int id) {
		show(new TagSettingsWindow(QuickTags.placement(id), QuickTags.name(id) + " Tag", () -> show(new QuickTagsWindow()), () -> QuickTags.delete(id)));
	}

	@Override
	protected void reopen() {
		show(new QuickTagsWindow());
	}
}
