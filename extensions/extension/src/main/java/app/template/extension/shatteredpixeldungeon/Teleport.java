package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The Teleport patch: teleporting the hero and other creatures from the editors, and a Teleport to... button in
 * the Adventuring Notes for travelling to any floor.
 */
@SuppressWarnings("unused")
public final class Teleport {

	private static final String TAG = "Teleport";
	static final int MAX_DEPTH = 26;
	private static final int BUTTON_HEIGHT = 18;
	private static final int GAP = 2;
	static final int ENTRANCE = -1;
	static final int EXIT = -2;

	private static final Map<Object, RedButton> buttons = Collections.synchronizedMap(new WeakHashMap<>());

	private Teleport() {
	}

	/**
	 * Replaced by the Teleport patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	/**
	 * Called at the end of WndJournal.NotesTab.updateList and layout. Adds a Teleport to... button below the notes.
	 */
	public static void afterNotesLayout(Object tab) {
		if (!isAvailable() || !(tab instanceof Component) || Dungeon.hero == null) return;
		try {
			Component notes = (Component) tab;
			Field gridField = notes.getClass().getDeclaredField("grid");
			gridField.setAccessible(true);
			Object grid = gridField.get(notes);
			if (!(grid instanceof Component)) return;

			RedButton button = buttons.get(notes);
			if (button == null) {
				button = new RedButton("Teleport to...", 7) {
					@Override
					public void onClick() {
						Window window = windowOf(this);
						if (window != null) window.hide();
						EditorWindow.show(new DepthPickerWindow());
					}
				};
				notes.add(button);
				buttons.put(notes, button);
			}
			float bottom = notes.top() + notes.height();
			((Component) grid).setRect(notes.left(), notes.top(), notes.width(), notes.height() - BUTTON_HEIGHT - GAP);
			button.setRect(notes.left(), bottom - BUTTON_HEIGHT, notes.width(), BUTTON_HEIGHT);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the Teleport to button", e);
		}
	}

	private static Window windowOf(Gizmo gizmo) {
		for (Gizmo current = gizmo; current != null; current = current.parent) {
			if (current instanceof Window) return (Window) current;
		}
		return null;
	}

	/**
	 * Takes the hero to [depth], arriving at its entrance or its exit.
	 */
	static void toDepth(int depth, int arrival) {
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		try {
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnDepth = Math.max(1, Math.min(MAX_DEPTH, depth));
			InterlevelScene.returnBranch = 0;
			InterlevelScene.returnPos = arrival;
			Game.switchScene(InterlevelScene.class);
		} catch (Throwable e) {
			Log.e(TAG, "Could not teleport to depth " + depth, e);
		}
	}
}
