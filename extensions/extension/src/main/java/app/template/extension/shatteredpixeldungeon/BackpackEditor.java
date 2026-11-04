package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Adds an Edit action to items while the backpack was opened from the inventory editor.
 */
@SuppressWarnings("unused")
public final class BackpackEditor {

	public static final String ACTION_EDIT = "MORPHE_EDIT";

	private static final Set<Window> editWindows = Collections.newSetFromMap(new WeakHashMap<>());
	private static boolean pendingEditWindow;

	private BackpackEditor() {
	}

	public static void open() {
		if (Dungeon.hero == null) return;
		WndBag bag = new WndBag(Dungeon.hero.belongings.backpack);
		editWindows.add(bag);
		Game.scene().addToFront(bag);
	}

	private static boolean isOpenEditWindow(Window window) {
		return window != null && window.parent != null && editWindows.contains(window);
	}

	/**
	 * Called at the start of the WndBag constructor, before it replaces the open bag window.
	 */
	public static void beforeBagCreated() {
		pendingEditWindow = isOpenEditWindow(WndBag.INSTANCE);
	}

	/**
	 * Called at the end of the WndBag constructor. Switching bag tabs keeps edit mode.
	 */
	public static void afterBagCreated(Window window) {
		if (pendingEditWindow) editWindows.add(window);
		pendingEditWindow = false;
	}

	/**
	 * Called before Item.actions returns.
	 */
	public static ArrayList<String> addEditAction(ArrayList<String> actions) {
		if (actions == null || !InventoryWindow.isAvailable()) return actions;
		if (!isOpenEditWindow(WndBag.INSTANCE)) return actions;
		if (!actions.contains(ACTION_EDIT)) actions.add(ACTION_EDIT);
		return actions;
	}

	/**
	 * Called at the start of Item.actionName. Returns null for actions it doesn't handle.
	 */
	public static String actionName(String action) {
		return ACTION_EDIT.equals(action) ? "Edit" : null;
	}

	/**
	 * Called at the start of Item.execute. Returns true when the action was handled.
	 */
	public static boolean execute(Object item, String action) {
		if (!ACTION_EDIT.equals(action) || !(item instanceof Item)) return false;
		Game.scene().addToFront(new ItemEditWindow((Item) item));
		return true;
	}
}
