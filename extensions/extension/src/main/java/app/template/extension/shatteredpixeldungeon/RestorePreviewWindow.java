package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * A save or undo step about to be put back into its slot, shown in full first, with Restore and Back buttons.
 */
public class RestorePreviewWindow extends PreviewWindow {

	private final String title;
	private final UndoScene scene;
	private final Hero hero;
	private final Runnable onRestore;
	private final Runnable onBack;

	RestorePreviewWindow(String title, UndoScene scene, Hero hero, Runnable onRestore, Runnable onBack) {
		super();
		this.title = title;
		this.scene = scene;
		this.hero = hero;
		this.onRestore = onRestore;
		this.onBack = onBack;
		layout(title, scene, hero);
	}

	@Override
	protected void addActions() {
		addButtonPair("Restore", this::restore, "Back", this::back);
		pos -= GAP;
	}

	private void restore() {
		ConfirmPrompt.ask("Restore", "Restore this to its slot and continue the game from it?\n\n" + title, "Restore", () -> {
			hide();
			onRestore.run();
		});
	}

	private void back() {
		hide();
		onBack.run();
	}

	@Override
	public void onBackPressed() {
		back();
	}

	@Override
	protected void reopen() {
		show(new RestorePreviewWindow(title, scene, hero, onRestore, onBack));
	}
}
