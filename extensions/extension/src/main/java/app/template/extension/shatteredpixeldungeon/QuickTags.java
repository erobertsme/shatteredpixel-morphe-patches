package app.template.extension.shatteredpixeldungeon;

/**
 * Tags on the screen edge that open an editor window or run an action when tapped. Styled and placed like item tags, and kept with each
 * game like them.
 */
public final class QuickTags {

	/**
	 * The quick tags. Each one's target is the id of its action, and it shows while that action exists.
	 */
	static final TagList LIST = new TagList("quicktag", "_action", TagPlacement.Side.LEFT, new TagList.Kind() {
		@Override
		public String name(String target) {
			QuickActions.Action action = QuickActions.find(target);
			return action == null ? "Missing action" : action.name;
		}

		@Override
		public String icon(String target) {
			QuickActions.Action action = QuickActions.find(target);
			return action == null ? "ARROW" : action.icon;
		}

		@Override
		public boolean shows(String target) {
			return QuickActions.find(target) != null;
		}

		@Override
		public void use(String target, IconTag tag) {
			QuickActions.Action action = QuickActions.find(target);
			if (action != null) action.run();
		}
	});

	private QuickTags() {
	}

	/**
	 * The action of the quick tag [id], or null when it no longer exists.
	 */
	static QuickActions.Action action(int id) {
		return QuickActions.find(LIST.target(id));
	}
}
