package app.template.extension.shatteredpixeldungeon;

/**
 * A save or undo step about to be put back into its slot, shown in full first, with Restore and Back buttons.
 */
public class RestorePreviewWindow extends PreviewWindow {

	private final GameSaves.Save save;
	private final Runnable onRestore;
	private final Runnable onBack;

	RestorePreviewWindow(GameSaves.Save save, Runnable onRestore, Runnable onBack) {
		super();
		this.save = save;
		this.onRestore = onRestore;
		this.onBack = onBack;
		layout(save);
	}

	@Override
	protected void addActions() {
		backTo(onBack);
		addButtonPair("Restore", this::restore, "Back", this::goBack);
		pos -= GAP;
	}

	private void restore() {
		ConfirmPrompt.ask("Restore", "Restore this to its slot and continue the game from it?\n\n" + save.title(), "Restore", () -> {
			hide();
			onRestore.run();
		});
	}

	@Override
	protected void reopen() {
		show(new RestorePreviewWindow(save, onRestore, onBack));
	}
}
