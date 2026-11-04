package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Game;

import java.util.List;

public class StatusPickerWindow extends Window {

	private static final int WIDTH = CharacterWindow.WIDTH;
	private static final int ROW_HEIGHT = CharacterWindow.ROW_HEIGHT;
	private static final int GAP = CharacterWindow.GAP;
	private static final int INFO_SIZE = CharacterWindow.INFO_SIZE;
	private static final Buff.buffType[] KINDS = { Buff.buffType.POSITIVE, Buff.buffType.NEGATIVE, Buff.buffType.NEUTRAL };
	private static final String[] KIND_NAMES = { "Positive", "Negative", "Other" };

	private static int duration = 20;
	private static boolean infinite;

	private final Buff.buffType kind;
	private final int page;
	private float pos;

	public StatusPickerWindow(Buff.buffType kind, int page) {
		super();
		this.kind = kind;
		this.page = page;
		Hero hero = Dungeon.hero;

		RenderedTextBlock title = PixelScene.renderTextBlock("Add status", 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.setPos(0, 0);
		add(title);
		pos = title.bottom() + 2 * GAP;

		float width = (WIDTH - (KINDS.length - 1) * GAP) / (float) KINDS.length;
		for (int i = 0; i < KINDS.length; i++) {
			Buff.buffType option = KINDS[i];
			RedButton button = new RedButton(KIND_NAMES[i], 6) {
				@Override
				public void onClick() {
					if (option != kind) reopen(option, 0);
				}
			};
			if (option == kind) button.textColor(CharacterWindow.TITLE_COLOR);
			button.setRect(i * (width + GAP), pos, width, ROW_HEIGHT);
			add(button);
		}
		pos += ROW_HEIGHT + 2 * GAP;

		addDuration();
		addInfinite();

		List<StatusEditor.Entry> entries = StatusEditor.catalog(kind);
		int perPage = PixelScene.landscape() ? 4 : 7;
		int pages = Math.max(1, (entries.size() + perPage - 1) / perPage);
		int current = Math.min(page, pages - 1);
		int end = Math.min(entries.size(), (current + 1) * perPage);
		for (int i = current * perPage; i < end; i++) addEntry(hero, entries.get(i));
		if (pages > 1) addPager(current, pages);

		addHint("Tap to add, i or long-press to read. Yellow = already active.");

		RedButton cancel = new RedButton("Back", 7) {
			@Override
			public void onClick() {
				back();
			}
		};
		cancel.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(cancel);
		pos += ROW_HEIGHT;

		resize(WIDTH, (int) pos);
	}

	private void addDuration() {
		RenderedTextBlock label = PixelScene.renderTextBlock("Duration", 7);
		label.setPos(0, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);

		float valueWidth = 40;
		float x = WIDTH - 2 * 15 - valueWidth - 2 * GAP;
		addStep("-", x, -5);
		RedButton value = new RedButton(duration + " turns", 6) {
			@Override
			public void onClick() {
				Game.scene().addToFront(new WndTextInput("Duration", null, String.valueOf(duration), 4, false, "Set", "Cancel") {
					@Override
					public void onSelect(boolean positive, String text) {
						if (!positive) return;
						try {
							setDuration(Integer.parseInt(text.trim()));
						} catch (NumberFormatException ignored) {
							return;
						}
						reopen(kind, page);
					}
				});
			}
		};
		value.setRect(x + 15 + GAP, pos, valueWidth, ROW_HEIGHT);
		add(value);
		addStep("+", value.right() + GAP, 5);
		pos += ROW_HEIGHT + GAP;
	}

	private void addInfinite() {
		CheckBox box = new CheckBox("Infinite") {
			@Override
			public void onClick() {
				super.onClick();
				infinite = checked();
			}
		};
		box.checked(infinite);
		box.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(box);
		pos += ROW_HEIGHT + GAP;
	}

	private void addStep(String label, float x, int delta) {
		RedButton button = new RedButton(label) {
			@Override
			public void onClick() {
				setDuration(duration + delta);
				reopen(kind, page);
			}

			@Override
			public boolean onLongClick() {
				setDuration(duration + delta * 10);
				reopen(kind, page);
				return true;
			}
		};
		button.setRect(x, pos, 15, ROW_HEIGHT);
		add(button);
	}

	private static void setDuration(int value) {
		duration = Math.max(1, Math.min(9999, value));
	}

	private void addEntry(Hero hero, StatusEditor.Entry entry) {
		RedButton button = new RedButton(entry.name, 6) {
			@Override
			public void onClick() {
				StatusEditor.add(hero, entry.type, duration);
				if (infinite) InfiniteStatuses.pin(entry.type, duration);
				back();
			}

			@Override
			public boolean onLongClick() {
				CharacterWindow.showBuffInfo(entry.sample);
				return true;
			}
		};
		try {
			button.icon(new BuffIcon(entry.sample, true));
		} catch (Throwable ignored) {
		}
		if (StatusEditor.isActive(hero, entry.type)) button.textColor(CharacterWindow.TITLE_COLOR);
		button.setRect(0, pos, WIDTH - INFO_SIZE - GAP, ROW_HEIGHT);
		add(button);

		IconButton info = new IconButton(Icons.get(Icons.INFO)) {
			@Override
			public void onClick() {
				CharacterWindow.showBuffInfo(entry.sample);
			}
		};
		info.setRect(WIDTH - INFO_SIZE, pos, INFO_SIZE, ROW_HEIGHT);
		add(info);
		pos += ROW_HEIGHT + GAP;
	}

	private void addPager(int current, int pages) {
		RedButton previous = new RedButton("<", 7) {
			@Override
			public void onClick() {
				reopen(kind, (current + pages - 1) % pages);
			}
		};
		previous.setRect(0, pos, 24, ROW_HEIGHT);
		add(previous);

		RenderedTextBlock label = PixelScene.renderTextBlock("Page " + (current + 1) + " / " + pages, 6);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);

		RedButton next = new RedButton(">", 7) {
			@Override
			public void onClick() {
				reopen(kind, (current + 1) % pages);
			}
		};
		next.setRect(WIDTH - 24, pos, 24, ROW_HEIGHT);
		add(next);
		pos += ROW_HEIGHT + GAP;
	}

	private void addHint(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.maxWidth(WIDTH);
		block.hardlight(CharacterWindow.DIM_COLOR);
		block.setPos(0, pos);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	private void reopen(Buff.buffType newKind, int newPage) {
		hide();
		Game.scene().addToFront(new StatusPickerWindow(newKind, newPage));
	}

	private void back() {
		hide();
		CharacterWindow.open(CharacterWindow.TAB_STATUS, 1);
	}
}
