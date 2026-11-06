package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
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
	private static final int FAVORITE_SIZE = 14;
	private static final int RESET_SIZE = 14;
	private static final int ROW_PADDING = 3;
	private static final int HEART_ICON = 21;
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
		addTitle(text, null);
	}

	/**
	 * The window title. When [info] is given, an [i] button in the top right corner shows it.
	 */
	protected void addTitle(String text, String info) {
		RenderedTextBlock title = PixelScene.renderTextBlock(text, 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.maxWidth(info == null ? WIDTH : WIDTH - InfoButton.SIZE - GAP);
		title.setPos(0, pos + (InfoButton.SIZE - title.height()) / 2f);
		add(title);
		if (info != null) InfoButton.add(this, WIDTH, pos, info);
		pos = Math.max(title.bottom(), pos + (info == null ? 0 : InfoButton.SIZE)) + 2 * GAP;
	}

	protected void addSection(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 7);
		block.hardlight(CharacterWindow.TITLE_COLOR);
		block.setPos(0, pos + 2 * GAP);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	/**
	 * Builds content with [build] only to measure its height, then removes it again.
	 */
	@SuppressWarnings("unchecked")
	protected float measure(Runnable build) {
		float start = pos;
		java.util.List<com.watabou.noosa.Gizmo> members;
		try {
			java.lang.reflect.Field field = com.watabou.noosa.Group.class.getDeclaredField("members");
			field.setAccessible(true);
			members = (java.util.List<com.watabou.noosa.Gizmo>) field.get(this);
		} catch (ReflectiveOperationException e) {
			return 0;
		}
		int before = members.size();
		build.run();
		float height = pos - start;
		for (com.watabou.noosa.Gizmo gizmo : new java.util.ArrayList<>(members.subList(before, members.size()))) {
			remove(gizmo);
			gizmo.destroy();
		}
		pos = start;
		return height;
	}

	protected void addHint(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.maxWidth(WIDTH);
		block.hardlight(CharacterWindow.DIM_COLOR);
		block.setPos(0, pos);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	private Image nextStepperIcon;

	/**
	 * Shows [icon] to the left of the label of the next stepper.
	 */
	protected void stepperIcon(Image icon) {
		nextStepperIcon = icon;
	}

	protected void addStepper(String label, String display, int current, int step, int min, int max, IntSetter setter) {
		addStepper(label, display, current, step, min, max, setter, true);
	}

	protected void addStepper(String label, String display, int current, int step, int min, int max, IntSetter setter, boolean enabled) {
		addStepper(label, display, current, step, min, max, setter, enabled, null);
	}

	/**
	 * A stepper row. When [onReset] is given, a small reset button at the end of the row restores that one value.
	 */
	protected void addStepper(String label, String display, int current, int step, int min, int max, IntSetter setter, boolean enabled, Runnable onReset) {
		float textLeft = 0;
		Image icon = nextStepperIcon;
		nextStepperIcon = null;
		if (icon != null) {
			icon.x = 0;
			icon.y = pos + (ROW_HEIGHT - icon.height()) / 2f;
			PixelScene.align(icon);
			add(icon);
			textLeft = icon.width() + GAP;
		}
		RenderedTextBlock text = PixelScene.renderTextBlock(label, 7);
		text.setPos(textLeft, pos + (ROW_HEIGHT - text.height()) / 2f);
		if (!enabled) text.alpha(0.3f);
		add(text);

		float right = WIDTH;
		if (onReset != null) {
			IconButton reset = new IconButton(Icons.get(Icons.CHANGES)) {
				@Override
				public void onClick() {
					onReset.run();
					refresh();
				}
			};
			reset.setRect(WIDTH - RESET_SIZE, pos, RESET_SIZE, ROW_HEIGHT);
			reset.enable(enabled);
			add(reset);
			right = WIDTH - RESET_SIZE - GAP;
		}

		float x = right - 2 * STEP_WIDTH - VALUE_WIDTH - 2 * GAP;
		addStepButton("-", x, current, -step, setter).enable(enabled);

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
		value.enable(enabled);
		add(value);

		addStepButton("+", value.right() + GAP, current, step, setter).enable(enabled);
		pos += ROW_HEIGHT + GAP;
	}

	private RedButton addStepButton(String label, float x, int current, int delta, IntSetter setter) {
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
		return button;
	}

	protected void addCheckBox(String label, boolean checked, Runnable onToggle) {
		addCheckBox(label, checked, onToggle, true);
	}

	protected void addCheckBox(String label, boolean checked, Runnable onToggle, boolean enabled) {
		addCheckBox(label, checked, onToggle, enabled, 0);
	}

	protected void addCheckBox(String label, boolean checked, Runnable onToggle, boolean enabled, int textSize) {
		CheckBox box = new ToggleBox(label) {
			@Override
			public void onClick() {
				super.onClick();
				onToggle.run();
				refresh();
			}

			@Override
			public void layout() {
				super.layout();
				if (textSize <= 0 || text == null) return;
				String value = text.text();
				remove(text);
				text = PixelScene.renderTextBlock(value, textSize);
				float margin = (height() - text.height()) / 2f;
				text.setPos(left() + margin, top() + margin);
				PixelScene.align(text);
				add(text);
			}
		};
		box.checked(checked);
		box.enable(enabled);
		box.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(box);
		pos += ROW_HEIGHT + GAP;
	}

	protected RedButton addButton(String label, float x, float width, Runnable action) {
		RedButton button = new RedButton(TitleCase.of(label), 6) {
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

	interface Favorite {
		boolean isFavorite();

		void toggle();
	}

	protected void addListRow(String label, Image icon, boolean highlighted, boolean enabled, Runnable onClick, Runnable onInfo) {
		addListRow(label, icon, highlighted, enabled, onClick, onInfo, null);
	}

	protected void addListRow(String label, Image icon, boolean highlighted, boolean enabled, Runnable onClick, Runnable onInfo, Favorite favorite) {
		float x = 0;
		if (favorite != null) {
			boolean favorited = favorite.isFavorite();
			BuffIcon heart = new BuffIcon(HEART_ICON, true);
			if (!favorited) heart.alpha(0.3f);
			IconButton heartButton = new IconButton(heart) {
				@Override
				public void onClick() {
					favorite.toggle();
					refresh();
				}
			};
			heartButton.setRect(0, pos, FAVORITE_SIZE, ROW_HEIGHT);
			add(heartButton);
			x = FAVORITE_SIZE + GAP;
		}

		float width = (onInfo == null ? WIDTH : WIDTH - INFO_SIZE - GAP) - x;
		RedButton button = new RedButton("") {
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
		button.setRect(x, pos, width, ROW_HEIGHT);
		button.enable(enabled);
		add(button);

		float textLeft = x + ROW_PADDING;
		if (icon != null) {
			icon.x = x + ROW_PADDING;
			icon.y = pos + (ROW_HEIGHT - icon.height()) / 2f;
			if (!enabled) icon.alpha(0.3f);
			add(icon);
			textLeft = icon.x + icon.width() + ROW_PADDING;
		}

		RenderedTextBlock text = PixelScene.renderTextBlock(label, 6);
		text.maxWidth((int) (x + width - textLeft - ROW_PADDING));
		if (highlighted) text.hardlight(CharacterWindow.TITLE_COLOR);
		if (!enabled) text.alpha(0.3f);
		text.setPos(textLeft, pos + (ROW_HEIGHT - text.height()) / 2f);
		add(text);

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

	interface RowRenderer {
		void render(int index);
	}

	/**
	 * Shows one page of a list. The page always takes the height of a full page plus the page buttons,
	 * so the window keeps its size when a page has fewer rows.
	 */
	protected void addPagedList(int count, int perPage, int page, RowRenderer renderer, PageOpener opener) {
		int pages = pageCount(count, perPage);
		int current = Math.min(page, pages - 1);
		int start = current * perPage;
		int end = Math.min(count, start + perPage);
		for (int i = start; i < end; i++) renderer.render(i);
		pos += (perPage - (end - start)) * (ROW_HEIGHT + GAP);
		addPager(current, pages, opener);
	}

	protected void addPager(int current, int pages, PageOpener opener) {
		if (pages <= 1) {
			pos += ROW_HEIGHT + GAP;
			return;
		}
		addButton("", 0, 24, () -> opener.open((current + pages - 1) % pages)).icon(Icons.get(Icons.LEFTARROW));
		RenderedTextBlock label = PixelScene.renderTextBlock("Page " + (current + 1) + " / " + pages, 6);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		addButton("", WIDTH - 24, 24, () -> opener.open((current + 1) % pages)).icon(Icons.get(Icons.RIGHTARROW));
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
