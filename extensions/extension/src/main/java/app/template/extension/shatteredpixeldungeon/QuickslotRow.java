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
			if (SPDSettings.quickSwapper()) return;
			Component bar = (Component) toolbar;
			tallToolbars.add(toolbar);
			bar.setSize(bar.width(), bar.height() + ROW_HEIGHT);
		} catch (Throwable e) {
			Log.e(TAG, "Could not make room for the extra quickslots", e);
		}
	}

	/**
	 * Called at the end of Toolbar.layout. With the quickslot swapper on, the swap button pages through all twelve
	 * slots in place. Otherwise the toolbar moves down a row and the extra quickslots go above the ones the game
	 * shows.
	 */
	public static void afterToolbarLayout(Object toolbar) {
		try {
			if (!isOn() || QuickSlot.SIZE < GAME_SLOTS * 2) return;
			Component bar = (Component) toolbar;
			Object[] quick = (Object[]) field(bar, "btnQuick").get(bar);
			if (SPDSettings.quickSwapper() && SPDSettings.interfaceSize() == 0) {
				pageWithSwapper(bar, quick);
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

	private static int swapPage;
	private static Boolean lastSwapped;

	/**
	 * Shows the swap button beside the quickslots, making room by setting the last slot aside if needed, and fills
	 * the visible spots with the page of slots the swap button has reached.
	 */
	private static void pageWithSwapper(Component bar, Object[] quick) throws ReflectiveOperationException {
		Component swap = (Component) field(bar, "btnSwap").get(bar);
		List<Component> spots = new ArrayList<>();
		for (Object tool : quick) {
			Component slot = (Component) tool;
			if (slot.visible) spots.add(slot);
		}
		if (spots.isEmpty()) return;
		spots.sort((a, b) -> Float.compare(a.left(), b.left()));
		boolean flipped = SPDSettings.flipToolbar();
		if (!flipped) Collections.reverse(spots);

		if (!swap.visible) {
			float needed = swap.width() - 2;
			if (roomBeside(bar, spots.get(spots.size() - 1), flipped) < needed && spots.size() > 1) {
				hide(spots.remove(spots.size() - 1));
			}
			Component end = spots.get(spots.size() - 1);
			float x = flipped ? end.right() - 2 : end.left() - needed;
			swap.setPos(x, end.top() + 1);
			swap.visible = true;
			enable(swap, end.active);
		}

		Field swappedField = Class.forName("com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar").getDeclaredField("swappedQuickslots");
		swappedField.setAccessible(true);
		boolean swapped = swappedField.getBoolean(null);
		int size = spots.size();
		int pages = (quick.length + size - 1) / size;
		if (lastSwapped == null) {
			swapPage = 0;
		} else if (swapped != lastSwapped) {
			swapPage = (swapPage + 1) % pages;
		}
		lastSwapped = swapped;
		swapPage = Math.min(swapPage, pages - 1);

		float[][] places = new float[size][];
		for (int k = 0; k < size; k++) places[k] = new float[] { spots.get(k).left(), spots.get(k).top() };
		int start = swapPage * size;
		boolean[] used = new boolean[quick.length];
		for (int k = 0; k < size && start + k < quick.length; k++) {
			Component target = (Component) quick[start + k];
			copyLook(spots.get(k), target, places[k][0], places[k][1]);
			target.visible = true;
			enable(target, spots.get(k).active);
			used[start + k] = true;
		}
		for (int i = 0; i < quick.length; i++) {
			if (!used[i]) hide((Component) quick[i]);
		}
	}

	/**
	 * The free space beside [end], on the side the swap button goes, up to the next toolbar button or the edge.
	 */
	@SuppressWarnings("unchecked")
	private static float roomBeside(Component bar, Component end, boolean flipped) throws ReflectiveOperationException {
		float limit = flipped ? bar.right() : bar.left();
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		for (Gizmo member : (ArrayList<Gizmo>) membersField.get(bar)) {
			if (!(member instanceof Component) || member == end || !member.visible) continue;
			Component other = (Component) member;
			if (other.top() >= PixelScene.uiCamera.height - 1) continue;
			if (flipped && other.left() >= end.right() - 1) limit = Math.min(limit, other.left());
			if (!flipped && other.right() <= end.left() + 1) limit = Math.max(limit, other.right());
		}
		return flipped ? limit - end.right() : end.left() - limit;
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
