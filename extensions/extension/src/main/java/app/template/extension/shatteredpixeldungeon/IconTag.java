package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.noosa.Image;

/**
 * A toolbar tag drawn like a quickslot, with an icon and an action, placed and coloured by a [TagPlacement].
 */
class IconTag extends Button {

	static final int WIDTH = 22;
	static final int HEIGHT = 24;
	private static final String TOOLBAR = "interfaces/toolbar.png";
	private static final int SLOT_X = 64;
	private static final float BASE_R = 114 / 255f;
	private static final float BASE_G = 118 / 255f;
	private static final float BASE_B = 106 / 255f;
	private static final float DISABLED_ALPHA = 0.35f;
	private static final float PRESSED = 1.3f;

	private final Image base;
	private final Image icon;
	private final Runnable action;
	private boolean usable = true;
	private int color = TagPlacement.DEFAULT_COLOR;
	private boolean pressed;

	IconTag(Image icon, Runnable action) {
		super();
		this.base = slotImage();
		this.icon = icon;
		this.action = action;
		add(base);
		add(icon);
		setSize(WIDTH, HEIGHT);
		applyColor();
	}

	/**
	 * The quickslot box from the toolbar, tinted to [color]. The box's light and dark edges keep their contrast.
	 */
	static Image slotImage() {
		return new Image(TOOLBAR, SLOT_X, 0, WIDTH, HEIGHT);
	}

	static void tint(Image image, int color, float boost) {
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;
		image.hardlight(r / BASE_R * boost, g / BASE_G * boost, b / BASE_B * boost);
	}

	void color(int value) {
		if (color == value) return;
		color = value;
		applyColor();
	}

	private void applyColor() {
		tint(base, color, pressed ? PRESSED : 1f);
	}

	void usable(boolean value) {
		if (usable == value) return;
		usable = value;
		icon.alpha(value ? 1f : DISABLED_ALPHA);
	}

	@Override
	public void onPointerDown() {
		pressed = true;
		applyColor();
	}

	@Override
	public void onPointerUp() {
		pressed = false;
		applyColor();
	}

	@Override
	public void onClick() {
		if (usable) action.run();
	}

	@Override
	public void layout() {
		super.layout();
		if (base == null) return;
		base.x = left();
		base.y = top();
		icon.x = left() + (width() - icon.width()) / 2f;
		icon.y = top() + (height() - icon.height()) / 2f;
		PixelScene.align(base);
		PixelScene.align(icon);
	}
}
