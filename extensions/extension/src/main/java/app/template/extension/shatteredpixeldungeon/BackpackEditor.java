package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.BitmapText;
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
	private static final int SHORTCUT_WIDTH = 24;
	private static final int SHORTCUT_HEIGHT = 12;

	private static final Set<Window> editWindows = Collections.newSetFromMap(new WeakHashMap<>());
	private static boolean pendingEditWindow;
	private static boolean openingEditWindow;
	private static boolean switchingEditTab;
	private static Runnable backAction;
	private static Bag editBag;

	private BackpackEditor() {
	}

	/**
	 * Opens the backpack editor and remembers where its Back button returns to.
	 */
	public static void open(Runnable back) {
		backAction = back;
		open();
	}

	static void open(Runnable back, Bag bag) {
		backAction = back;
		editBag = bag;
		open();
	}

	public static void open() {
		if (Dungeon.hero == null) return;
		Bag bag = editBag != null ? editBag : Dungeon.hero.belongings.backpack;
		openingEditWindow = true;
		try {
			Game.scene().addToFront(new WndBag(bag));
		} finally {
			openingEditWindow = false;
		}
	}

	private static boolean isOpenEditWindow(Window window) {
		return window != null && window.parent != null && editWindows.contains(window);
	}

	/**
	 * Called at the start of WndBag.onClick(Tab), which closes the bag before opening the new tab.
	 */
	public static void beforeTabSwitch(Window window) {
		switchingEditTab = editWindows.contains(window);
	}

	/**
	 * Called at the end of the WndBag constructor for edit windows, to remember which bag tab is open.
	 */
	private static void rememberBag() {
		try {
			Field field = WndBag.class.getDeclaredField("lastBag");
			field.setAccessible(true);
			Bag bag = (Bag) field.get(null);
			if (bag != null) editBag = bag;
		} catch (Throwable ignored) {
		}
	}

	/**
	 * Called at the start of the WndBag constructor, before it replaces the open bag window.
	 */
	public static void beforeBagCreated() {
		pendingEditWindow = openingEditWindow || switchingEditTab || isOpenEditWindow(WndBag.INSTANCE);
		switchingEditTab = false;
	}

	/**
	 * Called at the end of the WndBag constructor. Switching bag tabs keeps edit mode.
	 */
	public static void afterBagCreated(Window window) {
		if (pendingEditWindow) {
			editWindows.add(window);
			decorate(window);
			rememberBag();
		}
		addShortcut(window, pendingEditWindow);
		pendingEditWindow = false;
	}

	private static void decorate(Window window) {
		try {
			addBackButton(window);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the backpack editor back button", e);
		}
		try {
			markTitle(window);
		} catch (Throwable e) {
			Log.e(TAG, "Could not mark the backpack editor title", e);
		}
	}

	private static void addShortcut(Window window, boolean editing) {
		if (!EditorSwitches.inventory() || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		try {
			Field selectorField = WndBag.class.getDeclaredField("selector");
			selectorField.setAccessible(true);
			if (selectorField.get(window) != null) return;

			RedButton shortcut = new RedButton(editing ? "Add" : "Edit", 6) {
				@Override
				public void onClick() {
					window.hide();
					if (editing) {
						ItemPickerWindow.openFromBackpack();
					} else {
						Bag current = currentBag();
						open(() -> Game.scene().addToFront(new WndBag(current)), current);
					}
				}
			};
			shortcut.setRect(counterLeft(window) - SHORTCUT_WIDTH - 3, 1, SHORTCUT_WIDTH, SHORTCUT_HEIGHT);
			window.add(shortcut);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the backpack shortcut", e);
		}
	}

	private static Bag currentBag() {
		try {
			Field field = WndBag.class.getDeclaredField("lastBag");
			field.setAccessible(true);
			Bag bag = (Bag) field.get(null);
			if (bag != null) return bag;
		} catch (Throwable ignored) {
		}
		return Dungeon.hero.belongings.backpack;
	}

	@SuppressWarnings("unchecked")
	private static float counterLeft(Window window) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		Field widthField = Window.class.getDeclaredField("width");
		widthField.setAccessible(true);
		float left = widthField.getInt(window);
		for (Gizmo member : (ArrayList<Gizmo>) membersField.get(window)) {
			if (member instanceof BitmapText) left = Math.min(left, ((BitmapText) member).x);
		}
		return left;
	}

	@SuppressWarnings("unchecked")
	private static RenderedTextBlock findTitle(Window window) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		RenderedTextBlock title = null;
		for (Gizmo member : (ArrayList<Gizmo>) membersField.get(window)) {
			if (!(member instanceof RenderedTextBlock)) continue;
			RenderedTextBlock block = (RenderedTextBlock) member;
			if (title == null || block.left() < title.left()) title = block;
		}
		return title;
	}

	private static void markTitle(Window window) throws ReflectiveOperationException {
		RenderedTextBlock title = findTitle(window);
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
				Runnable action = backAction;
				backAction = null;
				if (action != null) {
					action.run();
				} else {
					InventoryWindow.open();
				}
			}
		};
		back.setRect(0, height + 2, width, BACK_HEIGHT);
		window.add(back);
		window.resize(width, height + 2 + BACK_HEIGHT);
	}

	/**
	 * Called before Item.actions returns.
	 */
	public static ArrayList<String> addEditAction(ArrayList<String> actions) {
		if (actions == null || !EditorSwitches.inventory()) return actions;
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
