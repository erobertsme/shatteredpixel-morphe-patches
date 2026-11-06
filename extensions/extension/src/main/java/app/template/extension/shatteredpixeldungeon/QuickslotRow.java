package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A second row of six quickslots above the toolbar. Items in the extra row are saved under their own key, so a
 * save still loads normally if the patch is removed.
 */
@SuppressWarnings("unused")
public final class QuickslotRow {

	private static final String TAG = "QuickslotRow";
	private static final String KEY = "qol_extra_quickslots";
	private static final String BUNDLE_SLOT = "morphe_quickslot";
	private static final int GAME_SLOTS = 6;
	private static final int ROW_HEIGHT = 24;

	private static final Map<Object, Integer> storing = Collections.synchronizedMap(new IdentityHashMap<>());
	private static final Map<Object, Integer> restoring = Collections.synchronizedMap(new IdentityHashMap<>());
	private static final Set<Object> tallToolbars = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
	private static int savedSize = GAME_SLOTS;

	private QuickslotRow() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && CloverlordSettings.get(KEY);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY, on);
	}

	/**
	 * Called at the start of AndroidLauncher.onCreate, before any quickslots exist.
	 */
	public static void onLaunch() {
		if (QolSettings.isAvailable()) QuickSlot.SIZE = GAME_SLOTS * 2;
	}

	/**
	 * Called at the start of Item.storeInBundle. Keeps an extra-row slot out of the game's own quickslot key.
	 */
	public static void beforeItemStore(Object item, Bundle bundle) {
		try {
			QuickSlot quickslot = Dungeon.quickslot;
			if (quickslot == null || bundle == null || !(item instanceof Item)) return;
			int slot = quickslot.getSlot((Item) item);
			if (slot < GAME_SLOTS) return;
			bundle.put(BUNDLE_SLOT, slot);
			quickslot.clearSlot(slot);
			storing.put(item, slot);
		} catch (Throwable e) {
			Log.e(TAG, "Could not save a quickslot", e);
		}
	}

	/**
	 * Called at the end of Item.storeInBundle.
	 */
	public static void afterItemStore(Object item) {
		Integer slot = storing.remove(item);
		if (slot == null || Dungeon.quickslot == null) return;
		Dungeon.quickslot.setSlot(slot, (Item) item);
	}

	/**
	 * Called at the start of Item.restoreFromBundle.
	 */
	public static void beforeItemRestore(Object item, Bundle bundle) {
		try {
			if (bundle == null || !bundle.contains(BUNDLE_SLOT)) return;
			restoring.put(item, bundle.getInt(BUNDLE_SLOT));
		} catch (Throwable e) {
			Log.e(TAG, "Could not read a quickslot", e);
		}
	}

	/**
	 * Called at the end of Item.restoreFromBundle.
	 */
	public static void afterItemRestore(Object item) {
		Integer slot = restoring.remove(item);
		if (slot == null || Dungeon.quickslot == null || slot >= QuickSlot.SIZE) return;
		Dungeon.quickslot.setSlot(slot, (Item) item);
	}

	/**
	 * Called at the start of QuickSlot.storePlaceholders. Only the game's own six slots keep placeholders.
	 */
	public static void beforePlaceholders() {
		savedSize = QuickSlot.SIZE;
		QuickSlot.SIZE = Math.min(savedSize, GAME_SLOTS);
	}

	/**
	 * Called at the end of QuickSlot.storePlaceholders.
	 */
	public static void afterPlaceholders() {
		QuickSlot.SIZE = savedSize;
	}

	/**
	 * Called at the end of the Toolbar constructor. Makes room for the extra row above the toolbar.
	 */
	public static void afterToolbarCreated(Object toolbar) {
		try {
			if (!isOn() || QuickSlot.SIZE < GAME_SLOTS * 2 || SPDSettings.interfaceSize() != 0) return;
			Component bar = (Component) toolbar;
			tallToolbars.add(toolbar);
			bar.setSize(bar.width(), bar.height() + ROW_HEIGHT);
		} catch (Throwable e) {
			Log.e(TAG, "Could not make room for the extra quickslots", e);
		}
	}

	/**
	 * Called at the end of Toolbar.layout. Moves the toolbar down to the bottom row and puts the extra quickslots
	 * above the ones the game shows.
	 */
	@SuppressWarnings("unchecked")
	public static void afterToolbarLayout(Object toolbar) {
		if (!tallToolbars.contains(toolbar)) return;
		try {
			Component bar = (Component) toolbar;
			float offscreen = PixelScene.uiCamera.height - 1;
			Field membersField = Group.class.getDeclaredField("members");
			membersField.setAccessible(true);
			for (Gizmo member : new ArrayList<>((ArrayList<Gizmo>) membersField.get(bar))) {
				if (!(member instanceof Component)) continue;
				Component component = (Component) member;
				if (component.top() >= offscreen) continue;
				component.setPos(component.left(), component.top() + ROW_HEIGHT);
			}

			Field quickField = bar.getClass().getDeclaredField("btnQuick");
			quickField.setAccessible(true);
			Object[] quick = (Object[]) quickField.get(bar);
			for (int i = 0; i < GAME_SLOTS && i + GAME_SLOTS < quick.length; i++) {
				Component shown = (Component) quick[i];
				Component extra = (Component) quick[i + GAME_SLOTS];
				if (!shown.visible) {
					extra.visible = false;
					extra.setPos(extra.left(), offscreen + 1);
					continue;
				}
				extra.setPos(shown.left(), shown.top() - ROW_HEIGHT);
				extra.visible = true;
				enable(extra, shown.active);
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not lay out the extra quickslots", e);
		}
	}

	private static void enable(Object tool, boolean value) {
		for (Class<?> type = tool.getClass(); type != null; type = type.getSuperclass()) {
			try {
				Method method = type.getDeclaredMethod("enable", boolean.class);
				method.setAccessible(true);
				method.invoke(tool, value);
				return;
			} catch (NoSuchMethodException ignored) {
			} catch (Throwable e) {
				return;
			}
		}
	}
}
