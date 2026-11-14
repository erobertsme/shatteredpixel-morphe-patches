package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Image;

import java.util.List;

/**
 * The Item Tags page in Extras, QoL: the tags you've made, and Add Tag.
 */
public class CustomTagsWindow extends EditorWindow {

	private static final String INFO = "Item Tags\n\nAdd Tag lets you pick an item from your backpack. A tag for it appears on the screen edge, and tapping the tag uses the item, like a quickslot. The tag follows the kind of item, using the first one you carry, and hides while you carry none. Each game keeps its own item tags.\n\nTap a tag in the list to change its position, size, colour, icon and opacity, or to delete it. Default Icon on the Icon tab shows the item again.";

	public CustomTagsWindow() {
		super();
		addTitle("Item Tags", INFO);
		List<Integer> ids = CustomItemTags.ids();
		if (ids.isEmpty()) addHint("No item tags yet.");
		for (int id : ids) {
			addListRow(CustomItemTags.name(id), icon(id), false, true, () -> {
				hide();
				openTag(id);
			}, null);
		}
		pos += GAP;
		boolean inGame = Dungeon.hero != null && Dungeon.hero.isAlive() && GameSettings.inGame();
		addButton("Add Tag", 0, WIDTH, this::addTag).enable(inGame);
		pos += ROW_HEIGHT + GAP;
		if (!inGame) addHint("Open this during a game to add tags.");
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private static Image icon(int id) {
		String className = CustomItemTags.itemClass(id);
		Item item = CustomItemTags.held(className);
		if (item == null) item = CustomItemTags.sample(className);
		try {
			return item == null ? Icons.get(Icons.BACKPACK_LRG) : new ItemSprite(item);
		} catch (Throwable e) {
			return null;
		}
	}

	static void openTag(int id) {
		show(new TagSettingsWindow(CustomItemTags.placement(id), CustomItemTags.name(id) + " Tag",
			() -> show(new CustomTagsWindow()), () -> CustomItemTags.delete(id)));
	}

	private void addTag() {
		hide();
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return "Choose an item for the tag";
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item.defaultAction() != null;
			}

			@Override
			public void onSelect(Item item) {
				if (item == null) {
					show(new CustomTagsWindow());
					return;
				}
				openTag(CustomItemTags.add(item));
			}
		});
	}

	@Override
	protected void reopen() {
		show(new CustomTagsWindow());
	}
}
