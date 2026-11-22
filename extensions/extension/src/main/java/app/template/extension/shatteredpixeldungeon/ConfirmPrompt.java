package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

/**
 * A question with Cancel and one or more actions. A single action sits beside Cancel; several actions are stacked
 * above it.
 */
public class ConfirmPrompt extends EditorWindow {

	private static final int SCREEN_MARGIN = 8;

	private final String title;
	private final String message;
	private final String[] labels;
	private final Runnable[] actions;
	private final String closeLabel;
	private Runnable onClose;

	public ConfirmPrompt(String title, String message, String confirmLabel, Runnable onConfirm) {
		this(title, message, new String[] { confirmLabel }, new Runnable[] { onConfirm });
	}

	public ConfirmPrompt(String title, String message, String[] labels, Runnable[] actions) {
		this(title, message, labels, actions, "Cancel");
	}

	/**
	 * A choice between [labels], with a last button labeled [closeLabel] that just closes the prompt.
	 */
	public ConfirmPrompt(String title, String message, String[] labels, Runnable[] actions, String closeLabel) {
		super();
		this.title = title;
		this.message = message;
		this.labels = labels;
		this.actions = actions;
		this.closeLabel = closeLabel;

		addTitle(title);
		RenderedTextBlock text = PixelScene.renderTextBlock(message, 6);
		text.maxWidth(WIDTH);
		text.setPos(0, pos);
		add(text);
		pos = text.bottom() + 2 * GAP;

		if (labels.length == 1) {
			float half = (WIDTH - GAP) / 2f;
			addButton("Cancel", 0, half, this::hide);
			addButton(labels[0], half + GAP, half, () -> choose(0));
			pos += ROW_HEIGHT;
			finish();
			return;
		}
		for (int i = 0; i < labels.length; i++) {
			int index = i;
			addRowButton(labels[i], null, () -> choose(index));
		}
		addButton(closeLabel, 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private void choose(int index) {
		hide();
		actions[index].run();
	}

	static void ask(String title, String message, String confirmLabel, Runnable onConfirm) {
		show(new ConfirmPrompt(title, message, confirmLabel, onConfirm));
	}

	static void ask(String title, String message, String[] labels, Runnable[] actions) {
		show(new ConfirmPrompt(title, message, labels, actions));
	}

	/**
	 * Offers [labels], with a last button labeled [closeLabel] that just closes the prompt.
	 */
	static void ask(String title, String message, String[] labels, Runnable[] actions, String closeLabel) {
		show(new ConfirmPrompt(title, message, labels, actions, closeLabel));
	}

	/**
	 * Asks about a tile on the map. The window moves to the top or bottom of the screen, away from the tile, and
	 * [onClose] runs however the window closes.
	 */
	static void askAboutTile(int cell, String title, String message, String confirmLabel, Runnable onConfirm, Runnable onClose) {
		askAboutTile(cell, title, message, new String[] { confirmLabel }, new Runnable[] { onConfirm }, onClose);
	}

	static void askAboutTile(int cell, String title, String message, String[] labels, Runnable[] actions, Runnable onClose) {
		ConfirmPrompt prompt = new ConfirmPrompt(title, message, labels, actions);
		prompt.onClose = onClose;
		prompt.moveAwayFrom(cell);
		show(prompt);
	}

	private void moveAwayFrom(int cell) {
		try {
			int width = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.width();
			float worldX = (cell % width + 0.5f) * 16;
			float worldY = (cell / width + 0.5f) * 16;
			com.watabou.utils.Point screen = com.watabou.noosa.Camera.main.cameraToScreen(worldX, worldY);
			com.watabou.noosa.Camera ui = PixelScene.uiCamera;
			float tileY = screen.y / ui.zoom;
			float room = ui.height / 2f - pos / 2f - SCREEN_MARGIN;
			if (room <= 0) return;
			offset(0, (int) (tileY > ui.height / 2f ? -room : room));
		} catch (Throwable ignored) {
		}
	}

	@Override
	public void hide() {
		super.hide();
		Runnable close = onClose;
		onClose = null;
		if (close != null) close.run();
	}

	@Override
	protected void reopen() {
		show(new ConfirmPrompt(title, message, labels, actions, closeLabel));
	}
}
