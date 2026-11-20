package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;


/**
 * Where on-screen buttons can go without covering the game's toolbar.
 */
final class ScreenLayout {

	private static final int GAP = 4;

	private static Object cachedScene;
	private static int cachedWidth;
	private static int cachedHeight;
	private static float[] cachedInsets;
	private static float cachedStatusTop;
	private static final int STATUS_SPACE = 40;

	private ScreenLayout() {
	}

	/**
	 * The lowest y on the UI camera that buttons can reach, just above the toolbar and any extra quickslot row.
	 */
	static float aboveToolbar() {
		float fallback = PixelScene.uiCamera.height - insets()[3] - QuickslotRow.toolbarHeight() - GAP;
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
	 * Where the game puts the top of its status pane: just below large cutouts and the top display margin, the same rule
	 * GameScene uses.
	 */
	static float statusTop() {
		refresh();
		return cachedStatusTop;
	}

	/**
	 * How far in from each screen edge buttons and tags must stay on the UI camera, as left, top, right and bottom. It covers the phone's
	 * notches and bars and the display margins. The top uses large cutouts only, the same as the game's status pane and menu buttons, so a
	 * small camera hole doesn't push top tags down; the other edges follow the game's getCommonInsets, like its own tags.
	 */
	static float[] insets() {
		refresh();
		return cachedInsets.clone();
	}

	/**
	 * Works out the insets again when the screen has been laid out since last time. Android looks them up from the window each time it's
	 * asked, and they only change when the screen is laid out again: a new scene, which rotating, resizing, fullscreen and the display
	 * margins all cause, or a new screen size.
	 */
	private static synchronized void refresh() {
		Object scene = Game.scene();
		if (cachedInsets != null && scene == cachedScene && Game.width == cachedWidth && Game.height == cachedHeight) return;
		cachedScene = scene;
		cachedWidth = Game.width;
		cachedHeight = Game.height;
		float zoom = PixelScene.defaultZoom;
		float largeTop = 0;
		try {
			largeTop = Game.platform.getSafeInsets(PlatformSupport.INSET_LRG).top / zoom;
			cachedStatusTop = largeTop > 0 ? largeTop : SPDSettings.interfaceSize() == 0 ? -1 : 0;
		} catch (Throwable e) {
			cachedStatusTop = 0;
		}
		try {
			RectF all = Game.platform.getSafeInsets(PlatformSupport.INSET_ALL);
			RectF blocking = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK);
			cachedInsets = new float[] { blocking.left / zoom, largeTop, blocking.right / zoom, all.bottom / zoom };
		} catch (Throwable e) {
			cachedInsets = new float[4];
		}
	}
}
