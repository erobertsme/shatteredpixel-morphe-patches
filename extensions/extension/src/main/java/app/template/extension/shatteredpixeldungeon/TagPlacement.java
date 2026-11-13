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

	private int defaultOffset = -1;
	private int defaultOpacity = 100;
	private static final int AUTO_SIDE = 99;
	private final String defaultIcon;
	private final int defaultRotation;
	private final int defaultColor;
	private final boolean autoPlacement;
	private Runnable listener;
	private boolean autoByDefault;
	private boolean styled = true;

	/**
	 * Keeps the tag in its automatic place until it is moved, for one of the game's own tags.
	 */
	TagPlacement autoByDefault() {
		autoByDefault = true;
		return this;
	}

	/**
	 * Marks a tag whose look the game draws itself, so only its position and colour can be changed.
	 */
	TagPlacement positionAndColorOnly() {
		styled = false;
		return this;
	}

	boolean styled() {
		return styled;
	}

	/**
	 * The edges these tags can sit on. The game's own tags only sit on the left or right edge.
	 */
	Side[] sides() {
		return styled ? Side.values() : new Side[] { Side.LEFT, Side.RIGHT };
	}

	/**
	 * Whether the colour was chosen, rather than left as the tag's own.
	 */
	boolean colorChosen() {
		return (CloverlordSettings.getInt(key + "_color") & COLOR_SET) != 0;
	}

	/**
	 * Runs [listener] whenever one of this placement's settings changes, so the tags it places are laid out again only then. Replaces any
	 * earlier listener.
	 */
	void onChange(Runnable listener) {
		this.listener = listener;
	}

	private void storeInt(String settingKey, int value) {
		CloverlordSettings.setInt(settingKey, value);
		changed();
	}

	private void storeString(String settingKey, String value) {
		CloverlordSettings.setString(settingKey, value);
		changed();
	}

	private void changed() {
		Runnable current = listener;
		if (current != null) current.run();
	}

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

	/**
	 * Starts the tags [pixels] along their edge until moved, instead of a share of the edge.
	 */
	TagPlacement defaultOffset(int pixels) {
		defaultOffset = pixels;
		return this;
	}

	TagPlacement defaultOpacity(int percent) {
		defaultOpacity = percent;
		return this;
	}

	boolean supportsAuto() {
		return autoPlacement;
	}

	/**
	 * Whether the tag keeps its default position, for a placement that has one.
	 */
	boolean isAuto() {
		if (!autoPlacement) return false;
		int stored = CloverlordSettings.getInt(key + "_side");
		return stored == AUTO_SIDE || autoByDefault && stored == 0;
	}

	/**
	 * Puts the tags back on their default edge, at their default distance along it.
	 */
	void resetPosition() {
		storeInt(key + "_side", 0);
		storeInt(key + "_offset", 0);
	}

	void setAuto() {
		storeInt(key + "_side", AUTO_SIDE);
		storeInt(key + "_offset", 0);
	}

	int defaultColor() {
		return defaultColor;
	}

	void side(Side side) {
		storeInt(key + "_side", side.ordinal() + 1);
	}

	int offset() {
		int max = maxOffset();
		int stored = CloverlordSettings.getInt(key + "_offset") - 1;
		int value = stored < 0 ? (defaultOffset >= 0 ? defaultOffset : Math.round(max * defaultShare)) : stored;
		return Math.max(0, Math.min(max, value));
	}

	void offset(int value) {
		storeInt(key + "_offset", Math.max(0, Math.min(maxOffset(), value)) + 1);
	}

	void resetOffset() {
		storeInt(key + "_offset", 0);
	}

	/**
	 * The largest offset that keeps every tag on screen along the chosen edge.
	 */
	int maxOffset() {
		Camera ui = PixelScene.uiCamera;
		Side side = side();
		float[] insets = ScreenLayout.insets();
		float span = side == Side.TOP ? ui.width - insets[0] - insets[2] : ui.height - insets[1] - insets[3];
		float length = tagCount * (side == Side.TOP ? tagWidth() : tagHeight());
		return Math.max(0, (int) (span - length));
	}

	int color() {
		int stored = CloverlordSettings.getInt(key + "_color");
		return (stored & COLOR_SET) == 0 ? defaultColor : stored & 0xFFFFFF;
	}

	void color(int rgb) {
		storeInt(key + "_color", (rgb & 0xFFFFFF) | COLOR_SET);
	}

	void resetColor() {
		storeInt(key + "_color", 0);
	}

	String icon() {
		String stored = CloverlordSettings.getString(key + "_icon");
		return stored == null || stored.isEmpty() ? defaultIcon : stored;
	}

	void resetIcon() {
		storeString(key + "_icon", "");
	}

	void icon(String name) {
		storeString(key + "_icon", name);
	}

	/**
	 * The icon's tint, or -1 for its own colours.
	 */
	int iconColor() {
		int stored = CloverlordSettings.getInt(key + "_icon_color");
		return (stored & COLOR_SET) == 0 ? -1 : stored & 0xFFFFFF;
	}

	void iconColor(int rgb) {
		storeInt(key + "_icon_color", (rgb & 0xFFFFFF) | COLOR_SET);
	}

	void resetIconColor() {
		storeInt(key + "_icon_color", 0);
	}

	int iconRotation() {
		int stored = CloverlordSettings.getInt(key + "_icon_rotation") - 1;
		return stored < 0 ? defaultRotation : stored % 360;
	}

	void iconRotation(int degrees) {
		storeInt(key + "_icon_rotation", (((degrees % 360) + 360) % 360) + 1);
	}

	void resetIconRotation() {
		storeInt(key + "_icon_rotation", 0);
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
		storeInt(key + "_width", Math.max(MIN_SIZE, Math.min(MAX_SIZE, value)) + 1);
	}

	/**
	 * How tall the tag is on screen, in pixels.
	 */
	int tagHeight() {
		return storedSize("_height");
	}

	void tagHeight(int value) {
		storeInt(key + "_height", Math.max(MIN_SIZE, Math.min(MAX_SIZE, value)) + 1);
	}

	void resetSize() {
		storeInt(key + "_width", 0);
		storeInt(key + "_height", 0);
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
		storeInt(key + "_icon_scale", Math.max(MIN_ICON_SCALE, Math.min(MAX_ICON_SCALE, value)) + 1);
	}

	void resetIconScale() {
		storeInt(key + "_icon_scale", 0);
	}

	/**
	 * Opacity of the tag's base, from 0 to 100.
	 */
	int tagOpacity() {
		int stored = CloverlordSettings.getInt(key + "_tag_opacity") - 1;
		return stored < 0 ? defaultOpacity : Math.min(100, stored);
	}

	void tagOpacity(int value) {
		int clamped = Math.max(0, Math.min(100, value));
		storeInt(key + "_tag_opacity", clamped + 1);
		if (opacityLinked()) storeInt(key + "_icon_opacity", clamped + 1);
	}

	/**
	 * Opacity of the tag's icon, from 0 to 100.
	 */
	int iconOpacity() {
		int stored = CloverlordSettings.getInt(key + "_icon_opacity") - 1;
		return stored < 0 ? defaultOpacity : Math.min(100, stored);
	}

	void iconOpacity(int value) {
		int clamped = Math.max(0, Math.min(100, value));
		storeInt(key + "_icon_opacity", clamped + 1);
		if (opacityLinked()) storeInt(key + "_tag_opacity", clamped + 1);
	}

	boolean opacityLinked() {
		return !CloverlordSettings.get(key + "_opacity_unlinked");
	}

	void opacityLinked(boolean linked) {
		CloverlordSettings.set(key + "_opacity_unlinked", !linked);
		if (linked) iconOpacity(tagOpacity());
	}

	void resetOpacity() {
		storeInt(key + "_tag_opacity", 0);
		storeInt(key + "_icon_opacity", 0);
	}

	/**
	 * Where tag number [index] of this group sits on screen, as x, y, width and height.
	 */
	float[] rect(int index) {
		Camera ui = PixelScene.uiCamera;
		float[] insets = ScreenLayout.insets();
		Side side = side();
		float start = offset();
		int width = tagWidth();
		int height = tagHeight();
		if (side == Side.TOP) return new float[] { insets[0] + start + index * width, insets[1], width, height };
		float x = side == Side.LEFT ? insets[0] : ui.width - insets[2] - width;
		return new float[] { x, insets[1] + start + index * height, width, height };
	}

	/**
	 * Where along the chosen edge a point on the UI camera falls, as an offset, measured the way [offset] is.
	 */
	float offsetAt(float x, float y) {
		float[] insets = ScreenLayout.insets();
		return side() == Side.TOP ? x - insets[0] : y - insets[1];
	}

	int tagCount() {
		return tagCount;
	}

	/**
	 * Lines [tags] up along the chosen edge, in order, and styles them.
	 */
	void place(IconTag... tags) {
		Side side = side();
		for (int i = 0; i < tags.length; i++) {
			IconTag tag = tags[i];
			float[] rect = rect(i);
			tag.onTop(side == Side.TOP);
			tag.setRect(rect[0], rect[1], rect[2], rect[3]);
			tag.flip(side == Side.LEFT);
			tag.style(this);
		}
	}
}
