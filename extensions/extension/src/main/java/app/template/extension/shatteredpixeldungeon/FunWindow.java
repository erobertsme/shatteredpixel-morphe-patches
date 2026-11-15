package app.template.extension.shatteredpixeldungeon;

/**
 * The Fun patch's options.
 */
public class FunWindow extends EditorWindow {

	private static final String INFO = "Fun\n\nOptions that are just for fun.\n\nHero Sprite: make your hero look like another hero, an enemy or an NPC. Only the look changes; your hero plays exactly the same. Chosen in a game, it applies to that game; chosen on the main menu, it's the look for new games.";

	public FunWindow() {
		super();
		addTitle("Fun", INFO);
		addButton("Hero Sprite", 0, WIDTH, () -> {
			hide();
			show(new HeroSpriteWindow(HeroSpriteWindow.pageOf(HeroSkin.current())));
		}).icon(HeroSkin.icon(HeroSkin.skins().get(0)));
		pos += ROW_HEIGHT + 2 * GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new FunWindow());
	}
}
