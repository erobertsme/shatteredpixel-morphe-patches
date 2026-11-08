package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.Camera;

/**
 * Where a group of toolbar tags sits on screen: along the left, top or right edge, a chosen number of pixels from
 * the start of that edge (the top for the left and right edges, the left end for the top edge). Settings pages add
 * the controls with EditorWindow.addPlacementSettings.
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
		float span = side() == Side.TOP ? ui.width : ui.height;
		return Math.max(0, (int) (span - tagCount * Tag.SIZE));
	}

	/**
	 * Lines [tags] up along the chosen edge, in order.
	 */
	void place(Tag... tags) {
		Camera ui = PixelScene.uiCamera;
		Side side = side();
		float start = offset();
		for (int i = 0; i < tags.length; i++) {
			Tag tag = tags[i];
			if (side == Side.TOP) {
				tag.setRect(start + i * Tag.SIZE, 0, Tag.SIZE, Tag.SIZE);
				tag.flip(false);
			} else {
				float x = side == Side.LEFT ? 0 : ui.width - Tag.SIZE;
				tag.setRect(x, start + i * Tag.SIZE, Tag.SIZE, Tag.SIZE);
				tag.flip(side == Side.LEFT);
			}
		}
	}
}
