package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;

import java.util.List;

/**
 * The Quantum Chest, 25 slots a page. It shows either what is in the chest, or the hero's bags for storing items.
 * Tapping an item shows its details with a button to move it; with Quick Move on, tapping moves it straight away.
 */
public class QuantumChestWindow extends EditorWindow {

	private static final String CHEST_INFO = "Quantum Chest"
		+ "\n\nEvery quantum chest opens the same storage, so whatever you put in one waits for you in all the others."
		+ "\n\nTap an item to see its details, with Take Out at the bottom. Store Items shows your bags so you can put things in."
		+ "\n\n{CHECKED}Quick Move: tap an item to move it straight away, without the details window. Long-press still shows the details."
		+ "\n\nThe chest holds 25 items a page. The number of pages is set in Extras, under QoL.";
	private static final String STORE_INFO = "Store Items"
		+ "\n\nEverything in your bags that can go in the chest. Equipped items and bags can't be stored."
		+ "\n\nTap an item to see its details, with Store Item at the bottom. Back returns to the chest."
		+ "\n\n{CHECKED}Quick Move: tap an item to store it straight away, and keep storing until you tap Back.";
	private static final int COLUMNS = 5;
	private static final int SLOT_SIZE = 24;
	private static final int SLOT_GAP = 1;
	private static final int SLOT_COLOR = 0x9953564D;
	private static final int LOCKED_COLOR = 0x44000000;
	private static final int ARROW_WIDTH = 30;
	private static final int TOGGLE_SIZE = 12;

	private final boolean storing;
	private final int page;

	public QuantumChestWindow(int page) {
		this(false, page);
	}

	public QuantumChestWindow(boolean storing, int page) {
		super();
		this.storing = storing;
		List<Item> items = storing ? QuantumChest.storable() : QuantumChest.items();
		int perPage = QuantumChest.PER_PAGE;
		int pages = storing
			? Math.max(1, (items.size() + perPage - 1) / perPage)
			: Math.max(QuantumChest.pages(), (items.size() + perPage - 1) / perPage);
		this.page = Math.max(0, Math.min(pages - 1, page));

		addHeader(storing ? "Store Items" : "Quantum Chest", storing ? STORE_INFO : CHEST_INFO);

		float left = (WIDTH - (COLUMNS * SLOT_SIZE + (COLUMNS - 1) * SLOT_GAP)) / 2f;
		int start = this.page * perPage;
		int capacity = QuantumChest.capacity();
		for (int i = 0; i < perPage; i++) {
			int index = start + i;
			float x = left + (i % COLUMNS) * (SLOT_SIZE + SLOT_GAP);
			float y = pos + (i / COLUMNS) * (SLOT_SIZE + SLOT_GAP);
			boolean open = storing || index < capacity;
			ColorBlock background = new ColorBlock(SLOT_SIZE, SLOT_SIZE, open ? SLOT_COLOR : LOCKED_COLOR);
			background.x = x;
			background.y = y;
			add(background);
			if (index < items.size()) addSlot(items.get(index), x, y);
		}
		pos += (perPage / COLUMNS) * (SLOT_SIZE + SLOT_GAP) + GAP;

		if (pages > 1) addPager(pages);

		RenderedTextBlock count = PixelScene.renderTextBlock((storing ? "In the chest: " : "") + QuantumChest.items().size() + " / " + capacity + " items", 6);
		count.hardlight(CharacterWindow.DIM_COLOR);
		count.setPos((WIDTH - count.width()) / 2f, pos);
		add(count);
		pos = count.bottom() + 2 * GAP;

		if (storing) {
			addButton("Back", 0, WIDTH, () -> replaceWith(new QuantumChestWindow(false, 0)));
		} else {
			addButton("Store Items", 0, WIDTH, () -> replaceWith(new QuantumChestWindow(true, 0)));
		}
		pos += ROW_HEIGHT;
		finish();
	}

	private void addHeader(String text, String info) {
		RenderedTextBlock title = PixelScene.renderTextBlock(text, 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.setPos(0, pos + (InfoButton.SIZE - title.height()) / 2f);
		add(title);
		InfoButton.add(this, WIDTH, pos, info);

		boolean quick = QuantumChest.quickMove();
		RenderedTextBlock label = PixelScene.renderTextBlock("Quick Move", 6);
		if (!quick) label.alpha(0.6f);
		Image box = Icons.get(Icons.CHECKED);
		if (!quick) box.alpha(0.25f);
		IconButton toggle = new IconButton(box) {
			@Override
			public void onClick() {
				QuantumChest.quickMove(!QuantumChest.quickMove());
				refresh();
			}
		};
		float toggleRight = WIDTH - InfoButton.SIZE - GAP;
		toggle.setRect(toggleRight - TOGGLE_SIZE, pos + (InfoButton.SIZE - TOGGLE_SIZE) / 2f, TOGGLE_SIZE, TOGGLE_SIZE);
		add(toggle);
		label.setPos(toggle.left() - label.width() - 1, pos + (InfoButton.SIZE - label.height()) / 2f);
		add(label);
		pos += InfoButton.SIZE + 2 * GAP;
	}

	private void addSlot(Item item, float x, float y) {
		ItemSlot slot = new ItemSlot(item) {
			@Override
			public void onClick() {
				if (QuantumChest.quickMove()) {
					move(item);
					refresh();
					return;
				}
				replaceWith(details(item));
			}

			@Override
			public boolean onLongClick() {
				replaceWith(details(item));
				return true;
			}
		};
		slot.setRect(x, y, SLOT_SIZE, SLOT_SIZE);
		add(slot);
	}

	private ChestItemWindow details(Item item) {
		int returnPage = page;
		boolean fromBags = storing;
		return new ChestItemWindow(item, fromBags ? "Store Item" : "Take Out", () -> move(item), () -> show(new QuantumChestWindow(fromBags, returnPage)));
	}

	private void move(Item item) {
		if (storing) {
			if (!QuantumChest.store(item)) GLog.w("The quantum chest is full.");
			return;
		}
		if (!QuantumChest.take(item)) GLog.w("You don't have room for that.");
	}

	private void addPager(int pages) {
		boolean fromBags = storing;
		RedButton previous = new RedButton("", 7) {
			@Override
			public void onClick() {
				replaceWith(new QuantumChestWindow(fromBags, (page - 1 + pages) % pages));
			}
		};
		previous.icon(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.LEFTARROW));
		previous.setRect(0, pos, ARROW_WIDTH, ROW_HEIGHT);
		add(previous);

		RedButton next = new RedButton("", 7) {
			@Override
			public void onClick() {
				replaceWith(new QuantumChestWindow(fromBags, (page + 1) % pages));
			}
		};
		next.icon(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.RIGHTARROW));
		next.setRect(WIDTH - ARROW_WIDTH, pos, ARROW_WIDTH, ROW_HEIGHT);
		add(next);

		RenderedTextBlock label = PixelScene.renderTextBlock((page + 1) + "/" + pages, 8);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		pos += ROW_HEIGHT + GAP;
	}

	@Override
	protected void reopen() {
		show(new QuantumChestWindow(storing, page));
	}
}
