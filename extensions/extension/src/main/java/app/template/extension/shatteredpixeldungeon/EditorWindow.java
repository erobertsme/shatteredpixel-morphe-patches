package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

/**
 * Shared layout helpers for the editor windows. Every edit rebuilds the window through [reopen].
 */
abstract class EditorWindow extends Window {

	static final int WIDTH = CharacterWindow.WIDTH;
	static final int ROW_HEIGHT = CharacterWindow.ROW_HEIGHT;
	static final int GAP = CharacterWindow.GAP;
	static final int INFO_SIZE = CharacterWindow.INFO_SIZE;
	private static final int STEP_WIDTH = 15;
	private static final int VALUE_WIDTH = 40;

	interface IntSetter {
		void set(int value);
	}

	interface PageOpener {
		void open(int page);
	}

	protected float pos;

	protected abstract void reopen();

	protected void addTitle(String text) {
		RenderedTextBlock title = PixelScene.renderTextBlock(text, 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.maxWidth(WIDTH);
		title.setPos(0, pos);
		add(title);
		pos = title.bottom() + 2 * GAP;
	}

	protected void addSection(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 7);
		block.hardlight(CharacterWindow.TITLE_COLOR);
		block.setPos(0, pos + GAP);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	protected void addHint(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.maxWidth(WIDTH);
		block.hardlight(CharacterWindow.DIM_COLOR);
		block.setPos(0, pos);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	protected void addStepper(String label, String display, int current, int step, int min, int max, IntSetter setter) {
		RenderedTextBlock text = PixelScene.renderTextBlock(label, 7);
		text.setPos(0, pos + (ROW_HEIGHT - text.height()) / 2f);
		add(text);

		float x = WIDTH - 2 * STEP_WIDTH - VALUE_WIDTH - 2 * GAP;
		addStepButton("-", x, current, -step, setter);

		RedButton value = new RedButton(display, 6) {
			@Override
			public void onClick() {
				NumberPrompt.show(label, current, min, max, setter::set, () -> {
					EditorWindow.this.hide();
					reopen();
				});
			}
		};
		value.setRect(x + STEP_WIDTH + GAP, pos, VALUE_WIDTH, ROW_HEIGHT);
		add(value);

		addStepButton("+", value.right() + GAP, current, step, setter);
		pos += ROW_HEIGHT + GAP;
	}

	private void addStepButton(String label, float x, int current, int delta, IntSetter setter) {
		RedButton button = new RedButton(label) {
			@Override
			public void onClick() {
				setter.set(current + delta);
				refresh();
			}

			@Override
			public boolean onLongClick() {
				setter.set(current + delta * 10);
				refresh();
				return true;
			}
		};
		button.setRect(x, pos, STEP_WIDTH, ROW_HEIGHT);
		add(button);
	}

	protected void addCheckBox(String label, boolean checked, Runnable onToggle) {
		CheckBox box = new CheckBox(label) {
			@Override
			public void onClick() {
				super.onClick();
				onToggle.run();
				refresh();
			}
		};
		box.checked(checked);
		box.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(box);
		pos += ROW_HEIGHT + GAP;
	}

	protected RedButton addButton(String label, float x, float width, Runnable action) {
		RedButton button = new RedButton(label, 6) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		button.setRect(x, pos, width, ROW_HEIGHT);
		add(button);
		return button;
	}

	protected void addButtonRow(String[] labels, int selected, PageOpener onSelect) {
		float width = (WIDTH - (labels.length - 1) * GAP) / (float) labels.length;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].isEmpty()) continue;
			final int index = i;
			RedButton button = addButton(labels[i], i * (width + GAP), width, () -> onSelect.open(index));
			if (i == selected) button.textColor(CharacterWindow.TITLE_COLOR);
		}
		pos += ROW_HEIGHT + GAP;
	}

	protected void addListRow(String label, Image icon, boolean highlighted, boolean enabled, Runnable onClick, Runnable onInfo) {
		RedButton button = new RedButton(label, 6) {
			@Override
			public void onClick() {
				onClick.run();
			}

			@Override
			public boolean onLongClick() {
				if (onInfo == null) return false;
				onInfo.run();
				return true;
			}
		};
		if (icon != null) button.icon(icon);
		if (highlighted) button.textColor(CharacterWindow.TITLE_COLOR);
		float width = onInfo == null ? WIDTH : WIDTH - INFO_SIZE - GAP;
		button.setRect(0, pos, width, ROW_HEIGHT);
		button.enable(enabled);
		add(button);

		if (onInfo != null) {
			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				public void onClick() {
					onInfo.run();
				}
			};
			info.setRect(WIDTH - INFO_SIZE, pos, INFO_SIZE, ROW_HEIGHT);
			add(info);
		}
		pos += ROW_HEIGHT + GAP;
	}

	protected void addPager(int current, int pages, PageOpener opener) {
		if (pages <= 1) return;
		addButton("<", 0, 24, () -> opener.open((current + pages - 1) % pages));
		RenderedTextBlock label = PixelScene.renderTextBlock("Page " + (current + 1) + " / " + pages, 6);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		addButton(">", WIDTH - 24, 24, () -> opener.open((current + 1) % pages));
		pos += ROW_HEIGHT + GAP;
	}

	protected static int pageCount(int size, int perPage) {
		return Math.max(1, (size + perPage - 1) / perPage);
	}

	protected static int perPage(int portrait, int landscape) {
		return PixelScene.landscape() ? landscape : portrait;
	}

	protected void finish() {
		resize(WIDTH, (int) pos);
	}

	protected void refresh() {
		hide();
		reopen();
	}

	protected void replaceWith(Window window) {
		hide();
		Game.scene().addToFront(window);
	}

	protected static void show(Window window) {
		Game.scene().addToFront(window);
	}
}
