package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Item tags: toolbar tags you add for items. Each one uses its item, like a quickslot, and has its own tag style.
 * A tag belongs to a kind of item and uses the first one of that kind you carry. It hides while you carry none.
 */
@SuppressWarnings("unused")
public final class CustomItemTags {

	private static final String TAG = "CustomItemTags";
	private static final String KEY_IDS = "itemtag_list";
	private static final String KEY_NEXT = "itemtag_next";
	static final String HELD_PREFIX = "HELD:";

	private static final Map<Integer, TagPlacement> placements = new HashMap<>();
	private static int version;

	private CustomItemTags() {
	}

	static List<Integer> ids() {
		List<Integer> ids = new ArrayList<>();
		String stored = CloverlordSettings.getString(KEY_IDS);
		if (stored == null || stored.isEmpty()) return ids;
		for (String part : stored.split(",")) {
			try {
				ids.add(Integer.parseInt(part.trim()));
			} catch (NumberFormatException ignored) {
			}
		}
		return ids;
	}

	private static void saveIds(List<Integer> ids) {
		StringBuilder text = new StringBuilder();
		for (int id : ids) {
			if (text.length() > 0) text.append(',');
			text.append(id);
		}
		CloverlordSettings.setString(KEY_IDS, text.toString());
		version++;
	}

	static int add(Item item) {
		int id = Math.max(1, CloverlordSettings.getInt(KEY_NEXT));
		CloverlordSettings.setInt(KEY_NEXT, id + 1);
		CloverlordSettings.setString(itemKey(id), item.getClass().getName());
		List<Integer> ids = ids();
		ids.add(id);
		saveIds(ids);
		return id;
	}

	static void delete(int id) {
		List<Integer> ids = ids();
		ids.remove(Integer.valueOf(id));
		saveIds(ids);
		placements.remove(id);
	}

	private static String itemKey(int id) {
		return "itemtag_" + id + "_item";
	}

	static String itemClass(int id) {
		String stored = CloverlordSettings.getString(itemKey(id));
		return stored == null ? "" : stored;
	}

	static synchronized TagPlacement placement(int id) {
		TagPlacement placement = placements.get(id);
		if (placement != null) return placement;
		int position = Math.max(0, ids().indexOf(id));
		float share = Math.min(0.9f, 0.15f + 0.1f * position);
		placement = new TagPlacement("itemtag_" + id, TagPlacement.Side.RIGHT, share, 1, HELD_PREFIX + itemClass(id), 0);
		placements.put(id, placement);
		return placement;
	}

	/**
	 * The first item of [className] you carry, or null.
	 */
	static Item held(String className) {
		Hero hero = Dungeon.hero;
		if (hero == null || className == null || className.isEmpty()) return null;
		try {
			for (Item item : hero.belongings) {
				if (item != null && item.getClass().getName().equals(className)) return item;
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not search your items", e);
		}
		return null;
	}

	/**
	 * An item of [className] to show when you carry none.
	 */
	static Item sample(String className) {
		try {
			return (Item) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Throwable e) {
			return null;
		}
	}

	static String name(int id) {
		String className = itemClass(id);
		Item item = held(className);
		if (item == null) item = sample(className);
		try {
			return item == null ? "Unknown item" : InventoryEditor.capitalize(item.name());
		} catch (Throwable e) {
			return "Unknown item";
		}
	}

	private static void use(int id) {
		Hero hero = Dungeon.hero;
		Item item = held(itemClass(id));
		if (hero == null || item == null || !hero.isAlive() || !hero.ready) return;
		try {
			item.execute(hero);
			Item.updateQuickslot();
		} catch (Throwable e) {
			Log.e(TAG, "Could not use " + item.getClass().getName(), e);
		}
	}

	/**
	 * Called at the end of GameScene.create. Adds a tag for each item tag, and keeps them in step with the list.
	 */
	public static void onSceneCreated(Object scene) {
		if (!QolSettings.isAvailable() || !(scene instanceof Scene)) return;
		Scene target = (Scene) scene;
		Gizmo controller = new Gizmo() {
			private final Map<Integer, IconTag> tags = new HashMap<>();
			private int built = -1;

			@Override
			public void update() {
				super.update();
				if (built != version) rebuild();
				Hero hero = Dungeon.hero;
				boolean alive = hero != null && hero.isAlive();
				for (Map.Entry<Integer, IconTag> entry : tags.entrySet()) {
					IconTag tag = entry.getValue();
					tag.visible = alive && held(itemClass(entry.getKey())) != null;
					if (tag.visible) placement(entry.getKey()).place(tag);
				}
			}

			private void rebuild() {
				built = version;
				for (IconTag tag : tags.values()) {
					tag.killAndErase();
					tag.destroy();
				}
				tags.clear();
				for (int id : ids()) {
					IconTag tag = new IconTag(() -> use(id));
					tag.camera = PixelScene.uiCamera;
					tag.visible = false;
					target.add(tag);
					tags.put(id, tag);
				}
			}
		};
		target.add(controller);
	}
}
