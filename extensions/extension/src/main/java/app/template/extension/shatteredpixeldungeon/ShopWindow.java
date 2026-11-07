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
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;

import java.util.List;

/**
 * The shopkeeper's menu: sell, talk, and buy back sold items, 8 to a page with an i button to examine each one.
 */
public class ShopWindow extends EditorWindow {

	private static final String TAG = "ShopWindow";
	private static final int PER_PAGE = 8;

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

		List<Item> items = shopkeeper.buybackItems;
		if (items != null && !items.isEmpty()) {
			addSection("Buy Back");
			int perPage = Math.min(PER_PAGE, items.size());
			addPagedList(items.size(), perPage, page, index -> {
				Item item = items.get(index);
				int price = item.value();
				addListRow(InventoryEditor.capitalize(item.title()) + " - " + price + " gold", new ItemSprite(item), false, Dungeon.gold >= price,
					() -> buyBack(item), () -> show(new WndInfoItem(item)));
			}, newPage -> replaceWith(new ShopWindow(shopkeeper, newPage)));
		}
		finish();
	}

	private void buyBack(Item item) {
		Hero hero = Dungeon.hero;
		if (hero == null || !shopkeeper.buybackItems.contains(item) || Dungeon.gold < item.value()) return;
		hide();
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
