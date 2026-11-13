package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;


/**
 * Where on-screen buttons can go without covering the game's toolbar.
 */
final class ScreenLayout {

	private static final int TOOLBAR_SPACE = 30;
	private static final int GAP = 4;
	private static final int STATUS_SPACE = 40;

	private ScreenLayout() {
	}

	/**
	 * The lowest y on the UI camera that buttons can reach, just above the toolbar and any extra quickslot row.
	 */
	static float aboveToolbar() {
		float fallback = PixelScene.uiCamera.height - insets()[3] - TOOLBAR_SPACE;
		Object toolbar = Reflect.gameScenePart("toolbar");
		return toolbar instanceof Component ? Math.min(fallback, ((Component) toolbar).top() - GAP) : fallback;
	}

	/**
	 * The highest y on the UI camera below the hero's status pane.
	 */
	static float belowStatus() {
		Object status = Reflect.gameScenePart("status");
		return status instanceof Component ? ((Component) status).bottom() + GAP : STATUS_SPACE;
	}

	/**
	 * How far in from each screen edge buttons and tags must stay on the UI camera, as left, top, right and bottom. It covers the phone's
	 * notches and bars and the display margins, the same way the game keeps its own tags clear of them.
	 */
	static float[] insets() {
		try {
			RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_ALL);
			float zoom = PixelScene.uiCamera.zoom;
			return new float[] { insets.left / zoom, insets.top / zoom, insets.right / zoom, insets.bottom / zoom };
		} catch (Throwable e) {
			return new float[4];
		}
	}
}
