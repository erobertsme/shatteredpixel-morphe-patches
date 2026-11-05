package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Examining a tile lists everything on it: creatures, every item in a pile, plants, traps and the tile itself.
 */
@SuppressWarnings("unused")
public final class TileContents {

	private static final String TAG = "TileContents";
	private static final String KEY_OFF = "qol_tile_contents_off";

	private TileContents() {
	}

	static boolean isOn() {
		return FloorWindow.isAvailable() && !CloverlordSettings.get(KEY_OFF);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_OFF, !on);
	}

	/**
	 * Called at the start of GameScene.examineCell. Returns true when the list was shown instead.
	 */
	public static boolean examine(Integer cell) {
		try {
			if (cell == null || !isOn()) return false;
			Level level = Dungeon.level;
			if (level == null || cell < 0 || cell >= level.length()) return false;
			if (!level.visited[cell] && !level.mapped[cell]) return false;
			if (objectsAt(cell).isEmpty()) return false;
			EditorWindow.show(new TileContentsWindow(cell));
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not list the tile", e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	static List<Object> objectsAt(int cell) {
		try {
			Method method = GameScene.class.getDeclaredMethod("getObjectsAtCell", int.class);
			method.setAccessible(true);
			List<Object> objects = (List<Object>) method.invoke(null, cell);
			return objects == null ? new ArrayList<>() : objects;
		} catch (Throwable e) {
			Log.e(TAG, "Could not read the tile", e);
			return new ArrayList<>();
		}
	}
}
