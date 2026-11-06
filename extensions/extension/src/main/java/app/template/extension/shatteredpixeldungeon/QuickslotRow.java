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
	 * Called at the end of the Toolbar constructor. Makes room for the extra row above the toolbar, unless the
	 * quickslot swapper is in use, where the swap button cycles through all twelve slots instead.
	 */
	public static void afterToolbarCreated(Object toolbar) {
		try {
			if (!isOn() || QuickSlot.SIZE < GAME_SLOTS * 2 || SPDSettings.interfaceSize() != 0) return;
			if (swapperShown()) return;
			Component bar = (Component) toolbar;
			tallToolbars.add(toolbar);
			bar.setSize(bar.width(), bar.height() + ROW_HEIGHT);
		} catch (Throwable e) {
			Log.e(TAG, "Could not make room for the extra quickslots", e);
		}
	}

	private static boolean swapperShown() {
		int shown = 4;
		if (PixelScene.uiCamera.width > 152) shown++;
		if (PixelScene.uiCamera.width > 170) shown++;
		return SPDSettings.quickSwapper() && shown < GAME_SLOTS;
	}

	/**
	 * Called at the end of Toolbar.layout. With a tall toolbar, moves the toolbar down to the bottom row and puts
	 * the extra quickslots above the ones the game shows. With the swapper, shows the set of three slots the swap
	 * button has reached.
	 */
	public static void afterToolbarLayout(Object toolbar) {
		try {
			if (!isOn() || QuickSlot.SIZE < GAME_SLOTS * 2) return;
			Component bar = (Component) toolbar;
			Object[] quick = (Object[]) field(bar, "btnQuick").get(bar);
			Object swap = field(bar, "btnSwap").get(bar);
			if (swap instanceof Component && ((Component) swap).visible) {
				showSwapGroup(quick);
				return;
			}
			if (tallToolbars.contains(toolbar)) addExtraRow(bar, quick);
		} catch (Throwable e) {
			Log.e(TAG, "Could not lay out the extra quickslots", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void addExtraRow(Component bar, Object[] quick) throws ReflectiveOperationException {
		float offscreen = PixelScene.uiCamera.height - 1;
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		for (Gizmo member : new ArrayList<>((ArrayList<Gizmo>) membersField.get(bar))) {
			if (!(member instanceof Component)) continue;
			Component component = (Component) member;
			if (component.top() >= offscreen) continue;
			component.setPos(component.left(), component.top() + ROW_HEIGHT);
		}
		for (int i = 0; i < GAME_SLOTS && i + GAME_SLOTS < quick.length; i++) {
			Component shown = (Component) quick[i];
			Component extra = (Component) quick[i + GAME_SLOTS];
			if (!shown.visible) {
				hide(extra);
				continue;
			}
			copyLook(shown, extra, shown.left(), shown.top() - ROW_HEIGHT);
			extra.visible = true;
			enable(extra, shown.active);
		}
	}

	private static final int SWAP_GROUP = 3;
	private static int swapGroup;
	private static Boolean lastSwapped;

	private static void showSwapGroup(Object[] quick) throws ReflectiveOperationException {
		Field swappedField = Class.forName("com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar").getDeclaredField("swappedQuickslots");
		swappedField.setAccessible(true);
		boolean swapped = swappedField.getBoolean(null);
		int groups = quick.length / SWAP_GROUP;
		if (lastSwapped == null) {
			swapGroup = swapped ? 1 : 0;
		} else if (swapped != lastSwapped) {
			swapGroup = (swapGroup + 1) % groups;
		}
		lastSwapped = swapped;

		int gameStart = swapped ? SWAP_GROUP : 0;
		int start = swapGroup * SWAP_GROUP;
		if (start == gameStart) return;
		Component[] spots = new Component[SWAP_GROUP];
		float[][] places = new float[SWAP_GROUP][];
		for (int k = 0; k < SWAP_GROUP; k++) {
			spots[k] = (Component) quick[gameStart + k];
			places[k] = new float[] { spots[k].left(), spots[k].top() };
		}
		for (int k = 0; k < SWAP_GROUP; k++) {
			Component target = (Component) quick[start + k];
			copyLook(spots[k], target, places[k][0], places[k][1]);
			target.visible = true;
			enable(target, spots[k].active);
		}
		for (int i = 0; i < quick.length; i++) {
			if (i >= start && i < start + SWAP_GROUP) continue;
			hide((Component) quick[i]);
		}
	}

	private static void hide(Component tool) {
		tool.visible = false;
		enable(tool, false);
		tool.setPos(tool.left(), PixelScene.uiCamera.height);
	}

	/**
	 * Gives [target] the same background frame, borders and size as [source], at the given position.
	 */
	private static void copyLook(Component source, Component target, float x, float y) {
		try {
			Field baseField = field(source, "base");
			Object sourceBase = baseField.get(source);
			Object targetBase = baseField.get(target);
			Method getFrame = sourceBase.getClass().getMethod("frame");
			Object frame = getFrame.invoke(sourceBase);
			targetBase.getClass().getMethod("frame", getFrame.getReturnType()).invoke(targetBase, frame);
			for (String border : new String[] { "borderLeft", "borderRight" }) {
				Field borderField = field(source, border);
				borderField.setInt(target, borderField.getInt(source));
			}
		} catch (Throwable e) {
			Log.e(TAG, "Could not copy a quickslot's look", e);
		}
		target.setRect(x, y, source.width(), source.height());
	}

	private static Field field(Object target, String name) throws NoSuchFieldException {
		for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
			}
		}
		throw new NoSuchFieldException(name);
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
