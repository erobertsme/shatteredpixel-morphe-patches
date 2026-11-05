package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;
import com.watabou.utils.Bundle;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Quantum Chest: a chest beside the exit of every boss floor once its boss is beaten. Every chest opens the
 * same storage, which is saved with the hero.
 */
@SuppressWarnings("unused")
public final class QuantumChest {

	private static final String TAG = "QuantumChest";
	private static final String KEY_OFF = "qol_quantum_chest_off";
	private static final String KEY_PAGES = "qol_quantum_chest_pages";
	private static final String BUNDLE_ITEMS = "morphe_quantum_chest";
	private static final String BUNDLE_BOSSES = "morphe_quantum_chest_bosses";
	static final int PER_PAGE = 25;
	static final int MAX_PAGES = 3;
	private static final int TILE = 16;

	private static final List<Item> items = new ArrayList<>();
	private static final Set<Integer> defeatedBosses = new HashSet<>();
	private static WeakReference<Object> owner = new WeakReference<>(null);
	private static WeakReference<ItemSprite> sprite = new WeakReference<>(null);
	private static int chestCell = -1;

	private QuantumChest() {
	}

	static boolean isAvailable() {
		return QolSettings.isAvailable();
	}

	static boolean isOn() {
		return isAvailable() && !CloverlordSettings.get(KEY_OFF);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_OFF, !on);
	}

	static int pages() {
		int pages = CloverlordSettings.getInt(KEY_PAGES);
		return pages <= 0 ? 1 : Math.min(MAX_PAGES, pages);
	}

	static void pages(int pages) {
		CloverlordSettings.setInt(KEY_PAGES, Math.max(1, Math.min(MAX_PAGES, pages)));
	}

	static int capacity() {
		return pages() * PER_PAGE;
	}

	/**
	 * The stored items for the current hero. A new game starts with an empty chest.
	 */
	static synchronized List<Item> items() {
		syncOwner();
		return items;
	}

	private static void syncOwner() {
		Hero hero = Dungeon.hero;
		if (hero == null || owner.get() == hero) return;
		owner = new WeakReference<>(hero);
		items.clear();
		defeatedBosses.clear();
	}

	/**
	 * Called at the start of Hero.storeInBundle.
	 */
	public static synchronized void onStore(Object hero, Bundle bundle) {
		try {
			if (bundle == null || owner.get() != hero) return;
			if (!items.isEmpty()) bundle.put(BUNDLE_ITEMS, new ArrayList<>(items));
			if (defeatedBosses.isEmpty()) return;
			int[] depths = new int[defeatedBosses.size()];
			int i = 0;
			for (int depth : defeatedBosses) depths[i++] = depth;
			bundle.put(BUNDLE_BOSSES, depths);
		} catch (Throwable e) {
			Log.e(TAG, "Could not save the quantum chest", e);
		}
	}

	/**
	 * Called at the start of Hero.restoreFromBundle.
	 */
	public static synchronized void onRestore(Object hero, Bundle bundle) {
		owner = new WeakReference<>(hero);
		items.clear();
		defeatedBosses.clear();
		try {
			if (bundle == null) return;
			if (bundle.contains(BUNDLE_ITEMS)) {
				for (Object stored : bundle.getCollection(BUNDLE_ITEMS)) {
					if (stored instanceof Item) items.add((Item) stored);
				}
			}
			if (bundle.contains(BUNDLE_BOSSES)) {
				for (int depth : bundle.getIntArray(BUNDLE_BOSSES)) defeatedBosses.add(depth);
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not load the quantum chest", e);
		}
	}

	/**
	 * Called at the start of GameScene.bossSlain.
	 */
	public static synchronized void onBossSlain() {
		try {
			if (!Dungeon.bossLevel()) return;
			syncOwner();
			defeatedBosses.add(Dungeon.depth);
		} catch (Throwable e) {
			Log.e(TAG, "Could not record the boss", e);
		}
	}

	private static synchronized boolean bossDefeated() {
		syncOwner();
		return defeatedBosses.contains(Dungeon.depth) || Statistics.deepestFloor > Dungeon.depth;
	}

	/**
	 * Whether the chest stands on this floor right now.
	 */
	static boolean isHere() {
		try {
			return isOn() && Dungeon.level != null && Dungeon.bossLevel() && chestCell >= 0 && bossDefeated();
		} catch (Throwable e) {
			return false;
		}
	}

	static int cell() {
		return chestCell;
	}

	/**
	 * Called at the end of GameScene.create. Places the chest sprite beside the exit of a boss floor.
	 */
	public static void onSceneCreated(Object scene) {
		chestCell = -1;
		sprite = new WeakReference<>(null);
		try {
			if (!isAvailable() || !(scene instanceof Scene) || Dungeon.level == null || !Dungeon.bossLevel()) return;
			chestCell = findCell(Dungeon.level);
			if (chestCell < 0) return;

			ItemSprite chest = new ItemSprite(ItemSpriteSheet.EBONY_CHEST);
			Level level = Dungeon.level;
			int width = level.width();
			chest.x = (chestCell % width) * TILE + (TILE - chest.width()) / 2f;
			chest.y = (chestCell / width) * TILE + (TILE - chest.height()) / 2f;
			chest.visible = isHere();
			heapLayer((Scene) scene).add(chest);
			sprite = new WeakReference<>(chest);

			boolean[] shown = { chest.visible };
			((Scene) scene).add(new Gizmo() {
				@Override
				public void update() {
					super.update();
					boolean here = isHere();
					chest.visible = here;
					if (here && !shown[0]) GLog.i("A quantum chest shimmers into being beside the exit.");
					shown[0] = here;
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Could not place the quantum chest", e);
		}
	}

	private static Group heapLayer(Scene scene) {
		try {
			Field field = scene.getClass().getDeclaredField("heaps");
			field.setAccessible(true);
			Object layer = field.get(scene);
			if (layer instanceof Group) return (Group) layer;
		} catch (Throwable ignored) {
		}
		return scene;
	}

	/**
	 * A floor tile beside the exit, the same one every time.
	 */
	private static int findCell(Level level) {
		int exit = level.exit();
		if (exit < 0) return -1;
		int width = level.width();
		int[][] rings = {
			{ -width, 1, width, -1 },
			{ -width - 1, -width + 1, width - 1, width + 1 },
			{ -2 * width, 2, 2 * width, -2 },
		};
		for (int[] ring : rings) {
			for (int offset : ring) {
				int cell = exit + offset;
				if (cell < 0 || cell >= level.length()) continue;
				if (Math.abs(cell % width - exit % width) > 2) continue;
				if (isFloor(level, cell)) return cell;
			}
		}
		return -1;
	}

	private static boolean isFloor(Level level, int cell) {
		int tile = level.map[cell];
		if (!level.passable[cell] || tile == FloorEditor.CHASM) return false;
		if (tile == 7 || tile == 8 || tile == 22 || tile == 37) return false;
		return FloorEditor.trapAt(cell) == null;
	}

	/**
	 * Called before a tap moves the hero. Opens the chest when the hero is next to it or on it.
	 */
	static boolean interceptTap(int cell) {
		if (cell != chestCell || !isHere()) return false;
		Hero hero = Dungeon.hero;
		if (hero == null) return false;
		int width = Dungeon.level.width();
		int distance = Math.max(Math.abs(hero.pos % width - cell % width), Math.abs(hero.pos / width - cell / width));
		if (distance > 1) return false;
		EditorWindow.show(new QuantumChestWindow(0));
		return true;
	}

	/**
	 * Moves [item] from the hero's bags into the chest. Returns false when the chest is full.
	 */
	static synchronized boolean store(Item item) {
		Hero hero = Dungeon.hero;
		if (hero == null || item == null) return false;
		List<Item> stored = items();
		Item match = null;
		if (item.stackable) {
			for (Item candidate : stored) {
				if (candidate.isSimilar(item)) {
					match = candidate;
					break;
				}
			}
		}
		if (match == null && stored.size() >= capacity()) return false;
		item.detachAll(hero.belongings.backpack);
		if (match != null) {
			match.quantity(match.quantity() + item.quantity());
		} else {
			stored.add(item);
		}
		Item.updateQuickslot();
		return true;
	}

	/**
	 * Moves [item] from the chest into the hero's bags. Returns false when there is no room.
	 */
	static synchronized boolean take(Item item) {
		Hero hero = Dungeon.hero;
		List<Item> stored = items();
		if (hero == null || !stored.contains(item)) return false;
		stored.remove(item);
		if (item.doPickUp(hero)) return true;
		stored.add(item);
		return false;
	}
}
