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
		try {
			float top = Game.platform.getSafeInsets(PlatformSupport.INSET_LRG).top / PixelScene.defaultZoom;
			if (top > 0) return top;
			return SPDSettings.interfaceSize() == 0 ? -1 : 0;
		} catch (Throwable e) {
			return 0;
		}
	}

	/**
	 * How far in from each screen edge buttons and tags must stay on the UI camera, as left, top, right and bottom. It covers the phone's
	 * notches and bars and the display margins, the same way the game's getCommonInsets keeps its own tags clear of them.
	 */
	static float[] insets() {
		try {
			RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_ALL);
			RectF blocking = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK);
			float zoom = PixelScene.defaultZoom;
			return new float[] { blocking.left / zoom, insets.top / zoom, blocking.right / zoom, insets.bottom / zoom };
		} catch (Throwable e) {
			return new float[4];
		}
	}
}
