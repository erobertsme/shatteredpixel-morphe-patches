package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import java.lang.reflect.Field;

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

	private static final String TAG = "BackpackEditor";
	private static final int EDIT_COLOR = 0xFF9944;
	private static final int BACK_HEIGHT = 16;

	private static final Set<Window> editWindows = Collections.newSetFromMap(new WeakHashMap<>());
	private static boolean pendingEditWindow;

	private BackpackEditor() {
	}

	public static void open() {
		if (Dungeon.hero == null) return;
		WndBag bag = new WndBag(Dungeon.hero.belongings.backpack);
		editWindows.add(bag);
		decorate(bag);
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
		if (pendingEditWindow) {
			editWindows.add(window);
			decorate(window);
		}
		pendingEditWindow = false;
	}

	private static void decorate(Window window) {
		try {
			markTitle(window);
			addBackButton(window);
		} catch (Throwable e) {
			Log.e(TAG, "Could not decorate the backpack editor", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void markTitle(Window window) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		RenderedTextBlock title = null;
		for (Gizmo member : (ArrayList<Gizmo>) membersField.get(window)) {
			if (!(member instanceof RenderedTextBlock)) continue;
			RenderedTextBlock block = (RenderedTextBlock) member;
			if (title == null || block.left() < title.left()) title = block;
		}
		if (title == null) return;
		title.text("[Editing] " + title.text());
		title.hardlight(EDIT_COLOR);
	}

	private static void addBackButton(Window window) throws ReflectiveOperationException {
		Field widthField = Window.class.getDeclaredField("width");
		Field heightField = Window.class.getDeclaredField("height");
		widthField.setAccessible(true);
		heightField.setAccessible(true);
		int width = widthField.getInt(window);
		int height = heightField.getInt(window);

		RedButton back = new RedButton("Back", 7) {
			@Override
			public void onClick() {
				window.hide();
				InventoryWindow.open();
			}
		};
		back.textColor(EDIT_COLOR);
		back.setRect(0, height + 2, width, BACK_HEIGHT);
		window.add(back);
		window.resize(width, height + 2 + BACK_HEIGHT);
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
