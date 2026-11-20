package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tags on the screen edge that open an editor window or run an action when tapped. Styled and placed like item tags, and kept with each
 * game like them.
 */
public final class QuickTags {

	private static final String KEY_IDS = "quicktag_list";
	private static final String KEY_NEXT = "quicktag_next";

	private static final Map<Integer, TagPlacement> placements = new HashMap<>();
	private static final Map<Integer, IconTag> tags = new HashMap<>();
	private static Scene shownScene;

	private QuickTags() {
	}

	static List<Integer> ids() {
		List<Integer> ids = new ArrayList<>();
		String stored = CloverlordSettings.getString(KEY_IDS);
		if (stored == null || stored.isEmpty()) return ids;
		for (String part : stored.split(",")) {
			try {
				ids.add(Integer.parseInt(part.trim()));
			} catch (NumberFormatException ignored) {
			}
		}
		return ids;
	}

	private static void saveIds(List<Integer> ids) {
		StringBuilder text = new StringBuilder();
		for (int id : ids) {
			if (text.length() > 0) text.append(',');
			text.append(id);
		}
		CloverlordSettings.setString(KEY_IDS, text.toString());
		rebuild();
	}

	/**
	 * Adds a tag for [action] and returns its id.
	 */
	static int add(QuickActions.Action action) {
		int id = Math.max(1, CloverlordSettings.getInt(KEY_NEXT));
		CloverlordSettings.setInt(KEY_NEXT, id + 1);
		CloverlordSettings.setString(actionKey(id), action.id);
		List<Integer> ids = ids();
		ids.add(id);
		saveIds(ids);
		return id;
	}

	static void delete(int id) {
		List<Integer> ids = ids();
		ids.remove(Integer.valueOf(id));
		saveIds(ids);
		placements.remove(id);
	}

	private static String actionKey(int id) {
		return "quicktag_" + id + "_action";
	}

	static QuickActions.Action action(int id) {
		return QuickActions.find(CloverlordSettings.getString(actionKey(id)));
	}

	static String name(int id) {
		QuickActions.Action action = action(id);
		return action == null ? "Missing action" : action.name;
	}

	static synchronized TagPlacement placement(int id) {
		TagPlacement placement = placements.get(id);
		if (placement != null) return placement;
		int position = Math.max(0, ids().indexOf(id));
		float share = Math.min(0.9f, 0.15f + 0.1f * position);
		QuickActions.Action action = action(id);
		placement = new TagPlacement("quicktag_" + id, TagPlacement.Side.LEFT, share, 1, action == null ? "ARROW" : action.icon, 0);
		placements.put(id, placement);
		return placement;
	}

	/**
	 * Called at the end of GameScene.create. Adds a tag for each quick tag.
	 */
	public static void onSceneCreated(Object scene) {
		tags.clear();
		shownScene = QolSettings.isAvailable() && scene instanceof Scene ? (Scene) scene : null;
		rebuild();
	}

	private static void rebuild() {
		Scene scene = shownScene;
		if (scene == null) return;
		for (IconTag tag : tags.values()) {
			tag.killAndErase();
			tag.destroy();
		}
		tags.clear();
		for (int id : ids()) {
			IconTag tag = new IconTag(() -> use(id));
			tag.camera = PixelScene.uiCamera;
			tag.visible = false;
			scene.add(tag);
			tags.put(id, tag);
			placement(id).onChange(() -> show(id));
		}
		refresh();
	}

	private static void use(int id) {
		QuickActions.Action action = action(id);
		if (action != null) action.run();
	}

	/**
	 * Shows the tags while the hero is alive and hides them otherwise. Runs when the scene is made and when the game ends.
	 */
	static void refresh() {
		for (int id : tags.keySet()) show(id);
	}

	private static void show(int id) {
		IconTag tag = tags.get(id);
		if (tag == null || tag.parent == null) return;
		Hero hero = Dungeon.hero;
		tag.visible = hero != null && hero.isAlive() && action(id) != null;
		if (tag.visible) placement(id).place(tag);
	}
}
