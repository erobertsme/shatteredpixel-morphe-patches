package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.ColorBlock;

import java.util.List;

/**
 * The inside of the Quantum Chest: 25 slots a page. Tap an item to take it, long-press to examine it.
 */
public class QuantumChestWindow extends EditorWindow {

	private static final String INFO = "Quantum Chest"
		+ "\n\nEvery quantum chest opens the same storage, so whatever you put in one waits for you in all the others."
		+ "\n\nTap an item to take it, or long-press it to examine it. Store Item picks something from your bags to put in."
		+ "\n\nThe chest holds 25 items a page. The number of pages is set in Extras, under QoL.";
	private static final int COLUMNS = 5;
	private static final int SLOT_SIZE = 24;
	private static final int SLOT_GAP = 1;
	private static final int SLOT_COLOR = 0x9953564D;
	private static final int ARROW_WIDTH = 30;

	private final int page;

	public QuantumChestWindow(int page) {
		super();
		List<Item> items = QuantumChest.items();
		int pages = Math.max(QuantumChest.pages(), (items.size() + QuantumChest.PER_PAGE - 1) / QuantumChest.PER_PAGE);
		this.page = Math.max(0, Math.min(pages - 1, page));

		addTitle("Quantum Chest", INFO);
		float left = (WIDTH - (COLUMNS * SLOT_SIZE + (COLUMNS - 1) * SLOT_GAP)) / 2f;
		int start = this.page * QuantumChest.PER_PAGE;
		int capacity = QuantumChest.capacity();
		for (int i = 0; i < QuantumChest.PER_PAGE; i++) {
			int index = start + i;
			float x = left + (i % COLUMNS) * (SLOT_SIZE + SLOT_GAP);
			float y = pos + (i / COLUMNS) * (SLOT_SIZE + SLOT_GAP);
			ColorBlock background = new ColorBlock(SLOT_SIZE, SLOT_SIZE, index < capacity ? SLOT_COLOR : 0x44000000);
			background.x = x;
			background.y = y;
			add(background);
			if (index >= items.size()) continue;
			addSlot(items.get(index), x, y);
		}
		pos += (QuantumChest.PER_PAGE / COLUMNS) * (SLOT_SIZE + SLOT_GAP) + GAP;

		if (pages > 1) addPager(pages);

		RenderedTextBlock count = PixelScene.renderTextBlock(items.size() + " / " + capacity + " items", 6);
		count.hardlight(CharacterWindow.DIM_COLOR);
		count.setPos((WIDTH - count.width()) / 2f, pos);
		add(count);
		pos = count.bottom() + 2 * GAP;

		addButton("Store Item", 0, WIDTH, this::chooseItem).enable(items.size() < capacity || hasStackable());
		pos += ROW_HEIGHT;
		finish();
	}

	private void addSlot(Item item, float x, float y) {
		ItemSlot slot = new ItemSlot(item) {
			@Override
			public void onClick() {
				if (!QuantumChest.take(item)) GLog.w("You don't have room for that.");
				refresh();
			}

			@Override
			public boolean onLongClick() {
				show(new WndInfoItem(item));
				return true;
			}
		};
		slot.setRect(x, y, SLOT_SIZE, SLOT_SIZE);
		add(slot);
	}

	private void addPager(int pages) {
		RedButton previous = new RedButton("<-", 7) {
			@Override
			public void onClick() {
				replaceWith(new QuantumChestWindow((page - 1 + pages) % pages));
			}
		};
		previous.setRect(0, pos, ARROW_WIDTH, ROW_HEIGHT);
		add(previous);

		RedButton next = new RedButton("->", 7) {
			@Override
			public void onClick() {
				replaceWith(new QuantumChestWindow((page + 1) % pages));
			}
		};
		next.setRect(WIDTH - ARROW_WIDTH, pos, ARROW_WIDTH, ROW_HEIGHT);
		add(next);

		RenderedTextBlock label = PixelScene.renderTextBlock((page + 1) + "/" + pages, 8);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);
		pos += ROW_HEIGHT + GAP;
	}

	private static boolean hasStackable() {
		for (Item item : QuantumChest.items()) {
			if (item.stackable) return true;
		}
		return false;
	}

	private void chooseItem() {
		hide();
		int current = page;
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return "Store in the quantum chest";
			}

			@Override
			public boolean itemSelectable(Item item) {
				return !(item instanceof Bag) && !item.isEquipped(Dungeon.hero);
			}

			@Override
			public void onSelect(Item item) {
				if (item != null && !QuantumChest.store(item)) GLog.w("The quantum chest is full.");
				show(new QuantumChestWindow(current));
			}
		});
	}

	@Override
	protected void reopen() {
		show(new QuantumChestWindow(page));
	}
}
