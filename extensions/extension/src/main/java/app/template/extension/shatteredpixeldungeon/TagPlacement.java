package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Camera;

/**
 * Where a group of toolbar tags sits on screen and what colour they are: along the left, top or right edge, a
 * chosen number of pixels from the start of that edge (the top for the left and right edges, the left end for the
 * top edge). TagSettingsWindow holds the controls.
 */
final class TagPlacement {

	enum Side {
		LEFT("Left"),
		TOP("Top"),
		RIGHT("Right");

		final String label;

		Side(String label) {
			this.label = label;
		}
	}

	static final int DEFAULT_COLOR = 0x72766A;
	private static final int COLOR_SET = 0x1000000;

	private final String key;
	private final Side defaultSide;
	private final float defaultShare;
	private final int tagCount;

	/**
	 * [key] prefixes the saved settings. The tags start [defaultShare] of the way along [defaultSide].
	 */
	TagPlacement(String key, Side defaultSide, float defaultShare, int tagCount) {
		this.key = key;
		this.defaultSide = defaultSide;
		this.defaultShare = defaultShare;
		this.tagCount = tagCount;
	}

	Side side() {
		int stored = CloverlordSettings.getInt(key + "_side") - 1;
		Side[] sides = Side.values();
		return stored < 0 || stored >= sides.length ? defaultSide : sides[stored];
	}

	void side(Side side) {
		CloverlordSettings.setInt(key + "_side", side.ordinal() + 1);
	}

	int offset() {
		int max = maxOffset();
		int stored = CloverlordSettings.getInt(key + "_offset") - 1;
		int value = stored < 0 ? Math.round(max * defaultShare) : stored;
		return Math.max(0, Math.min(max, value));
	}

	void offset(int value) {
		CloverlordSettings.setInt(key + "_offset", Math.max(0, Math.min(maxOffset(), value)) + 1);
	}

	void resetOffset() {
		CloverlordSettings.setInt(key + "_offset", 0);
	}

	/**
	 * The largest offset that keeps every tag on screen along the chosen edge.
	 */
	int maxOffset() {
		Camera ui = PixelScene.uiCamera;
		Side side = side();
		float span = side == Side.TOP ? ui.width : ui.height;
		float length = side == Side.TOP ? tagCount * IconTag.WIDTH : tagCount * IconTag.HEIGHT;
		return Math.max(0, (int) (span - length));
	}

	int color() {
		int stored = CloverlordSettings.getInt(key + "_color");
		return (stored & COLOR_SET) == 0 ? DEFAULT_COLOR : stored & 0xFFFFFF;
	}

	void color(int rgb) {
		CloverlordSettings.setInt(key + "_color", (rgb & 0xFFFFFF) | COLOR_SET);
	}

	void resetColor() {
		CloverlordSettings.setInt(key + "_color", 0);
	}

	/**
	 * Lines [tags] up along the chosen edge, in order, and colours them.
	 */
	void place(IconTag... tags) {
		Camera ui = PixelScene.uiCamera;
		Side side = side();
		float start = offset();
		int color = color();
		for (int i = 0; i < tags.length; i++) {
			IconTag tag = tags[i];
			tag.color(color);
			if (side == Side.TOP) {
				tag.setRect(start + i * IconTag.WIDTH, 0, IconTag.WIDTH, IconTag.HEIGHT);
			} else {
				float x = side == Side.LEFT ? 0 : ui.width - IconTag.WIDTH;
				tag.setRect(x, start + i * IconTag.HEIGHT, IconTag.WIDTH, IconTag.HEIGHT);
			}
		}
	}
}
