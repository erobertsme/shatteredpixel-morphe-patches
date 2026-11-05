package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

/**
 * A yes or no question with Cancel on the left and the action on the right.
 */
public class ConfirmPrompt extends EditorWindow {

	private final String title;
	private final String message;
	private final String confirmLabel;
	private final Runnable onConfirm;

	public ConfirmPrompt(String title, String message, String confirmLabel, Runnable onConfirm) {
		super();
		this.title = title;
		this.message = message;
		this.confirmLabel = confirmLabel;
		this.onConfirm = onConfirm;

		addTitle(title);
		RenderedTextBlock text = PixelScene.renderTextBlock(message, 6);
		text.maxWidth(WIDTH);
		text.setPos(0, pos);
		add(text);
		pos = text.bottom() + 2 * GAP;

		float half = (WIDTH - GAP) / 2f;
		addButton("Cancel", 0, half, this::hide);
		addButton(confirmLabel, half + GAP, half, () -> {
			hide();
			onConfirm.run();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	static void ask(String title, String message, String confirmLabel, Runnable onConfirm) {
		show(new ConfirmPrompt(title, message, confirmLabel, onConfirm));
	}

	@Override
	protected void reopen() {
		show(new ConfirmPrompt(title, message, confirmLabel, onConfirm));
	}
}
