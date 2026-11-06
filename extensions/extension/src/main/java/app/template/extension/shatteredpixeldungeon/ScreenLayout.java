package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ui.Component;

import java.lang.reflect.Field;

/**
 * Where on-screen buttons can go without covering the game's toolbar.
 */
final class ScreenLayout {

	private static final int TOOLBAR_SPACE = 30;
	private static final int GAP = 4;

	private ScreenLayout() {
	}

	/**
	 * The lowest y on the UI camera that buttons can reach, just above the toolbar and any extra quickslot row.
	 */
	static float aboveToolbar() {
		float fallback = PixelScene.uiCamera.height - TOOLBAR_SPACE;
		try {
			Field sceneField = GameScene.class.getDeclaredField("scene");
			sceneField.setAccessible(true);
			Object scene = sceneField.get(null);
			if (scene == null) return fallback;
			Field toolbarField = GameScene.class.getDeclaredField("toolbar");
			toolbarField.setAccessible(true);
			Object toolbar = toolbarField.get(scene);
			if (!(toolbar instanceof Component)) return fallback;
			return Math.min(fallback, ((Component) toolbar).top() - GAP);
		} catch (Throwable e) {
			return fallback;
		}
	}
}
