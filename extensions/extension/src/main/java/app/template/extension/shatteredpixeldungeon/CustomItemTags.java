package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.Image;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;

import java.lang.reflect.Field;
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

	private static int targetingTag = -1;
	private static Object targetingListener;
	private static Image targetMark;
	private static Image tagMark;

	/**
	 * Uses the tag's item, like tapping a quickslot. An item that needs a target aims at your last target, marked
	 * with a crosshair, and tapping the tag again throws or zaps at it.
	 */
	private static void use(int id, IconTag tag) {
		Hero hero = Dungeon.hero;
		Item item = held(itemClass(id));
		if (hero == null || item == null || !hero.isAlive() || !hero.ready) return;
		try {
			Char target = QuickSlotButton.lastTarget;
			if (targetingTag == id && isTargeting() && target != null) {
				int cell = QuickSlotButton.autoAim(target, item);
				clearTargeting();
				GameScene.handleCell(cell != -1 ? cell : target.pos);
				return;
			}
			clearTargeting();
			if (GameScene.cancel()) return;
			item.execute(hero);
			Item.updateQuickslot();
			if (item.usesTargeting) startTargeting(id, tag);
		} catch (Throwable e) {
			Log.e(TAG, "Could not use " + item.getClass().getName(), e);
		}
	}

	private static void startTargeting(int id, IconTag tag) {
		Char target = QuickSlotButton.lastTarget;
		Level level = Dungeon.level;
		if (target == null || !target.isAlive() || target.alignment == Char.Alignment.ALLY || target.sprite == null) return;
		if (level == null || target.pos < 0 || target.pos >= level.length() || !level.heroFOV[target.pos]) return;
		Object listener = currentListener();
		if (listener == null) return;
		targetingTag = id;
		targetingListener = listener;
		targetMark = Icons.get(Icons.TARGET);
		if (target.sprite.parent != null) target.sprite.parent.addToFront(targetMark);
		tagMark = Icons.get(Icons.TARGET);
		tagMark.camera = PixelScene.uiCamera;
		if (tag.parent != null) tag.parent.addToFront(tagMark);
		updateMarks(tag);
	}

	private static boolean isTargeting() {
		return targetingTag != -1 && targetingListener != null && currentListener() == targetingListener;
	}

	private static Object currentListener() {
		try {
			Field selectorField = GameScene.class.getDeclaredField("cellSelector");
			selectorField.setAccessible(true);
			Object selector = selectorField.get(null);
			return selector == null ? null : selector.getClass().getField("listener").get(selector);
		} catch (Throwable e) {
			return null;
		}
	}

	private static void updateMarks(IconTag tag) {
		Char target = QuickSlotButton.lastTarget;
		if (targetMark != null && target != null && target.sprite != null) {
			targetMark.x = target.sprite.x + (target.sprite.width() - targetMark.width()) / 2f;
			targetMark.y = target.sprite.y + (target.sprite.height() - targetMark.height()) / 2f;
		}
		if (tagMark != null && tag != null) {
			tagMark.x = tag.left() + (tag.width() - tagMark.width()) / 2f;
			tagMark.y = tag.top() + (tag.height() - tagMark.height()) / 2f;
		}
	}

	private static void clearTargeting() {
		targetingTag = -1;
		targetingListener = null;
		for (Image mark : new Image[] { targetMark, tagMark }) {
			if (mark == null) continue;
			mark.killAndErase();
			mark.destroy();
		}
		targetMark = null;
		tagMark = null;
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
					if (targetingTag == entry.getKey()) {
						if (isTargeting() && tag.visible) {
							updateMarks(tag);
						} else {
							clearTargeting();
						}
					}
				}
			}

			private void rebuild() {
				built = version;
				clearTargeting();
				for (IconTag tag : tags.values()) {
					tag.killAndErase();
					tag.destroy();
				}
				tags.clear();
				for (int id : ids()) {
					IconTag[] holder = new IconTag[1];
					IconTag tag = new IconTag(() -> use(id, holder[0]));
					holder[0] = tag;
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
