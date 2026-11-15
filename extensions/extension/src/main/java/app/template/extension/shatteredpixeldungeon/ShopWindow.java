package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The shopkeeper's menu: sell, talk, and buy back sold items, newest first, each with its price and how much is known about it.
 */
public class ShopWindow extends EditorWindow {

	private static final String TAG = "ShopWindow";
	private static final int PER_PAGE = 6;
	private static final int PER_PAGE_LANDSCAPE = 3;
	private static final int BUYBACK_ROW_HEIGHT = 22;
	private static final int PRICE_WIDTH = 34;
	private static final int ROW_PADDING = 3;
	private static final int FULL_COLOR = 0xFF5555;

	private final Shopkeeper shopkeeper;
	private final int page;

	public ShopWindow(Shopkeeper shopkeeper, int page) {
		super();
		this.shopkeeper = shopkeeper;
		this.page = page;
		CurrencyIndicator.showGold = true;

		addTitle(InventoryEditor.capitalize(shopkeeper.name()));
		RenderedTextBlock description = PixelScene.renderTextBlock(shopkeeper.description(), 6);
		description.maxWidth(WIDTH);
		description.setPos(0, pos);
		add(description);
		pos = description.bottom() + 2 * GAP;

		float half = (WIDTH - GAP) / 2f;
		addButton("Sell", 0, half, () -> {
			hide();
			Shopkeeper.sell();
		});
		addButton("Talk", half + GAP, half, () -> {
			hide();
			show(new WndTitledMessage(shopkeeper.sprite(), Messages.titleCase(shopkeeper.name()), shopkeeper.chatText()));
		});
		pos += ROW_HEIGHT + GAP;

		List<Item> sold = shopkeeper.buybackItems;
		if (sold != null && !sold.isEmpty()) {
			List<Item> items = new ArrayList<>(sold);
			Collections.reverse(items);
			addBuybackHeader(items.size());
			int perPage = Math.min(perPage(PER_PAGE, PER_PAGE_LANDSCAPE), items.size());
			addPagedList(items.size(), perPage, page, index -> addBuybackRow(items.get(index)),
				newPage -> replaceWith(new ShopWindow(shopkeeper, newPage)));
		}
		finish();
	}

	/**
	 * The Buy Back heading, with how many sold items the shop is holding out of the most it keeps.
	 */
	private void addBuybackHeader(int count) {
		float top = pos;
		addSection("Buy Back");
		RenderedTextBlock counter = PixelScene.renderTextBlock(count + "/" + Shopkeeper.MAX_BUYBACK_HISTORY + " items sold", 6);
		counter.hardlight(count >= Shopkeeper.MAX_BUYBACK_HISTORY ? FULL_COLOR : CharacterWindow.DIM_COLOR);
		counter.setPos(WIDTH - counter.width(), top + 2 * GAP + 1);
		PixelScene.align(counter);
		add(counter);
	}

	/**
	 * A sold item: a button with its picture, name and how much is known about it, and its price beside it. Tapping it shows the item
	 * with a Buy back button.
	 */
	private void addBuybackRow(Item item) {
		int price = item.value();
		float buttonWidth = WIDTH - PRICE_WIDTH - GAP;
		RedButton button = new RedButton("") {
			@Override
			public void onClick() {
				hide();
				ShopItemWindow.forBuyback(item, shopkeeper, page);
			}
		};
		button.setRect(0, pos, buttonWidth, BUYBACK_ROW_HEIGHT);
		add(button);

		ItemSprite icon = new ItemSprite(item);
		icon.x = ROW_PADDING;
		icon.y = pos + (BUYBACK_ROW_HEIGHT - icon.height()) / 2f;
		PixelScene.align(icon);
		add(icon);

		float textLeft = icon.x + icon.width() + ROW_PADDING;
		float textWidth = buttonWidth - textLeft - ROW_PADDING;
		ItemCondition condition = ItemCondition.of(item);
		RenderedTextBlock name = PixelScene.renderTextBlock(PreviewParts.fit(InventoryEditor.capitalize(item.title()), 6, textWidth), 6);
		float nameTop = condition == null ? pos + (BUYBACK_ROW_HEIGHT - name.height()) / 2f : pos + ROW_PADDING;
		name.setPos(textLeft, nameTop);
		PixelScene.align(name);
		add(name);
		if (condition != null) {
			RenderedTextBlock state = PixelScene.renderTextBlock(PreviewParts.fit(condition.label, 6, textWidth), 6);
			state.hardlight(condition.color);
			state.setPos(textLeft, name.bottom() + 1);
			PixelScene.align(state);
			add(state);
		}

		RenderedTextBlock amount = PixelScene.renderTextBlock(String.valueOf(price), 7);
		amount.hardlight(Dungeon.gold >= price ? 0xFFFFFF : FULL_COLOR);
		amount.setPos(WIDTH - amount.width(), pos + (BUYBACK_ROW_HEIGHT - amount.height()) / 2f);
		PixelScene.align(amount);
		add(amount);
		ItemSprite gold = new ItemSprite(ItemSpriteSheet.GOLD);
		gold.x = amount.left() - gold.width();
		gold.y = pos + (BUYBACK_ROW_HEIGHT - gold.height()) / 2f;
		PixelScene.align(gold);
		add(gold);
		pos += BUYBACK_ROW_HEIGHT + GAP;
	}

	/**
	 * Buys [item] back from [shopkeeper] when the hero can afford it, putting it in the backpack or at the hero's feet.
	 */
	static void buyBack(Shopkeeper shopkeeper, Item item) {
		Hero hero = Dungeon.hero;
		if (hero == null || !shopkeeper.buybackItems.contains(item) || Dungeon.gold < item.value()) return;
		try {
			GLog.i(Messages.get(shopkeeper, "buyback"));
			shopkeeper.buybackItems.remove(item);
			Dungeon.gold -= item.value();
			Statistics.goldCollected -= item.value();
			if (item instanceof MissileWeapon && item.isUpgradable()) {
				Buff.affect(hero, MissileWeapon.UpgradedSetTracker.class).levelThresholds.put(((MissileWeapon) item).setID, item.level());
			}
			if (!item.doPickUp(hero)) {
				Heap heap = Dungeon.level.drop(item, hero.pos);
				if (heap != null && heap.sprite != null) heap.sprite.drop();
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not buy back " + item.getClass().getName(), e);
		}
	}

	@Override
	public void hide() {
		super.hide();
		CurrencyIndicator.showGold = false;
	}

	@Override
	protected void reopen() {
		show(new ShopWindow(shopkeeper, page));
	}
}
