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

	static final int DEFAULT_COLOR = 0xA3A695;
	private static final int COLOR_SET = 0x1000000;

	private final String key;
	private final Side defaultSide;
	private final float defaultShare;
	private final int tagCount;

	private final String defaultIcon;
	private final int defaultRotation;
	private final int defaultColor;
	private final boolean autoPlacement;

	/**
	 * Like the other constructor, with the tag's own default colour, and [autoPlacement] for a tag whose default
	 * position is worked out elsewhere, such as next to the game's own tags. Choosing an edge overrides it.
	 */
	TagPlacement(String key, Side defaultSide, float defaultShare, int tagCount, String defaultIcon, int defaultRotation, int defaultColor, boolean autoPlacement) {
		this.key = key;
		this.defaultSide = defaultSide;
		this.defaultShare = defaultShare;
		this.tagCount = tagCount;
		this.defaultIcon = defaultIcon;
		this.defaultRotation = defaultRotation;
		this.defaultColor = defaultColor;
		this.autoPlacement = autoPlacement;
	}

	/**
	 * [key] prefixes the saved settings. The tags start [defaultShare] of the way along [defaultSide], and show
	 * [defaultIcon] turned by [defaultRotation] degrees until changed.
	 */
	TagPlacement(String key, Side defaultSide, float defaultShare, int tagCount, String defaultIcon, int defaultRotation) {
		this.defaultIcon = defaultIcon;
		this.defaultRotation = defaultRotation;
		this.defaultColor = DEFAULT_COLOR;
		this.autoPlacement = false;
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

	boolean supportsAuto() {
		return autoPlacement;
	}

	/**
	 * Whether the tag keeps its default position, for a placement that has one.
	 */
	boolean isAuto() {
		return autoPlacement && CloverlordSettings.getInt(key + "_side") == 0;
	}

	void setAuto() {
		CloverlordSettings.setInt(key + "_side", 0);
		CloverlordSettings.setInt(key + "_offset", 0);
	}

	int defaultColor() {
		return defaultColor;
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
		float length = tagCount * (side == Side.TOP ? tagWidth() : tagHeight());
		return Math.max(0, (int) (span - length));
	}

	int color() {
		int stored = CloverlordSettings.getInt(key + "_color");
		return (stored & COLOR_SET) == 0 ? defaultColor : stored & 0xFFFFFF;
	}

	void color(int rgb) {
		CloverlordSettings.setInt(key + "_color", (rgb & 0xFFFFFF) | COLOR_SET);
	}

	void resetColor() {
		CloverlordSettings.setInt(key + "_color", 0);
	}

	String icon() {
		String stored = CloverlordSettings.getString(key + "_icon");
		return stored == null || stored.isEmpty() ? defaultIcon : stored;
	}

	void resetIcon() {
		CloverlordSettings.setString(key + "_icon", "");
	}

	void icon(String name) {
		CloverlordSettings.setString(key + "_icon", name);
	}

	/**
	 * The icon's tint, or -1 for its own colours.
	 */
	int iconColor() {
		int stored = CloverlordSettings.getInt(key + "_icon_color");
		return (stored & COLOR_SET) == 0 ? -1 : stored & 0xFFFFFF;
	}

	void iconColor(int rgb) {
		CloverlordSettings.setInt(key + "_icon_color", (rgb & 0xFFFFFF) | COLOR_SET);
	}

	void resetIconColor() {
		CloverlordSettings.setInt(key + "_icon_color", 0);
	}

	int iconRotation() {
		int stored = CloverlordSettings.getInt(key + "_icon_rotation") - 1;
		return stored < 0 ? defaultRotation : stored % 360;
	}

	void iconRotation(int degrees) {
		CloverlordSettings.setInt(key + "_icon_rotation", (((degrees % 360) + 360) % 360) + 1);
	}

	void resetIconRotation() {
		CloverlordSettings.setInt(key + "_icon_rotation", 0);
	}

	static final int MIN_SIZE = 12;
	static final int MAX_SIZE = 64;
	static final int MIN_ICON_SCALE = 25;
	static final int MAX_ICON_SCALE = 400;

	/**
	 * How wide the tag is on screen, in pixels.
	 */
	int tagWidth() {
		return storedSize("_width");
	}

	void tagWidth(int value) {
		CloverlordSettings.setInt(key + "_width", Math.max(MIN_SIZE, Math.min(MAX_SIZE, value)) + 1);
	}

	/**
	 * How tall the tag is on screen, in pixels.
	 */
	int tagHeight() {
		return storedSize("_height");
	}

	void tagHeight(int value) {
		CloverlordSettings.setInt(key + "_height", Math.max(MIN_SIZE, Math.min(MAX_SIZE, value)) + 1);
	}

	void resetSize() {
		CloverlordSettings.setInt(key + "_width", 0);
		CloverlordSettings.setInt(key + "_height", 0);
	}

	private int storedSize(String suffix) {
		int stored = CloverlordSettings.getInt(key + suffix) - 1;
		return stored < MIN_SIZE ? com.shatteredpixel.shatteredpixeldungeon.ui.Tag.SIZE : Math.min(MAX_SIZE, stored);
	}

	/**
	 * The icon's size as a percentage of its normal size.
	 */
	int iconScale() {
		int stored = CloverlordSettings.getInt(key + "_icon_scale") - 1;
		return stored < MIN_ICON_SCALE ? 100 : Math.min(MAX_ICON_SCALE, stored);
	}

	void iconScale(int value) {
		CloverlordSettings.setInt(key + "_icon_scale", Math.max(MIN_ICON_SCALE, Math.min(MAX_ICON_SCALE, value)) + 1);
	}

	void resetIconScale() {
		CloverlordSettings.setInt(key + "_icon_scale", 0);
	}

	/**
	 * Opacity of the tag's base, from 0 to 100.
	 */
	int tagOpacity() {
		int stored = CloverlordSettings.getInt(key + "_tag_opacity") - 1;
		return stored < 0 ? 100 : Math.min(100, stored);
	}

	void tagOpacity(int value) {
		int clamped = Math.max(0, Math.min(100, value));
		CloverlordSettings.setInt(key + "_tag_opacity", clamped + 1);
		if (opacityLinked()) CloverlordSettings.setInt(key + "_icon_opacity", clamped + 1);
	}

	/**
	 * Opacity of the tag's icon, from 0 to 100.
	 */
	int iconOpacity() {
		int stored = CloverlordSettings.getInt(key + "_icon_opacity") - 1;
		return stored < 0 ? 100 : Math.min(100, stored);
	}

	void iconOpacity(int value) {
		int clamped = Math.max(0, Math.min(100, value));
		CloverlordSettings.setInt(key + "_icon_opacity", clamped + 1);
		if (opacityLinked()) CloverlordSettings.setInt(key + "_tag_opacity", clamped + 1);
	}

	boolean opacityLinked() {
		return !CloverlordSettings.get(key + "_opacity_unlinked");
	}

	void opacityLinked(boolean linked) {
		CloverlordSettings.set(key + "_opacity_unlinked", !linked);
		if (linked) iconOpacity(tagOpacity());
	}

	void resetOpacity() {
		CloverlordSettings.setInt(key + "_tag_opacity", 0);
		CloverlordSettings.setInt(key + "_icon_opacity", 0);
	}

	/**
	 * Lines [tags] up along the chosen edge, in order, and styles them.
	 */
	void place(IconTag... tags) {
		Camera ui = PixelScene.uiCamera;
		Side side = side();
		float start = offset();
		int width = tagWidth();
		int height = tagHeight();
		for (int i = 0; i < tags.length; i++) {
			IconTag tag = tags[i];
			tag.onTop(side == Side.TOP);
			if (side == Side.TOP) {
				tag.setRect(start + i * width, 0, width, height);
				tag.flip(false);
			} else {
				float x = side == Side.LEFT ? 0 : ui.width - width;
				tag.setRect(x, start + i * height, width, height);
				tag.flip(side == Side.LEFT);
			}
			tag.style(this);
		}
	}
}
