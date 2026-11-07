package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Examining a tile with more than one thing on it lists them all: creatures, every item in a pile, items that
 * landed in a grave or chest, plants and traps.
 */
@SuppressWarnings("unused")
public final class TileContents {

	private static final String TAG = "TileContents";
	private static final String KEY_OFF = "qol_tile_contents_off";

	private TileContents() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && !CloverlordSettings.get(KEY_OFF);
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
			if (FloorEditSession.isActive()) return false;
			if (countThings(objectsAt(cell)) < 2) return false;
			EditorWindow.show(new TileContentsWindow(cell));
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not list the tile", e);
			return false;
		}
	}

	/**
	 * How many separate things the list would show: each item in a pile and each item that landed in a container
	 * counts on its own.
	 */
	static int countThings(List<Object> objects) {
		int count = 0;
		for (Object object : objects) {
			if (!(object instanceof Heap)) {
				count++;
				continue;
			}
			Heap heap = (Heap) object;
			if (heap.type == Heap.Type.HEAP && heap.items != null) {
				count += heap.items.size();
			} else {
				count += 1 + absorbedIn(heap).size();
			}
		}
		return count;
	}

	static List<Item> absorbedIn(Heap heap) {
		List<Item> result = new ArrayList<>();
		if (heap.items == null || !AbsorbedItems.isContainer(heap)) return result;
		for (Item item : heap.items) {
			if (AbsorbedItems.isAbsorbed(item)) result.add(item);
		}
		return result;
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
