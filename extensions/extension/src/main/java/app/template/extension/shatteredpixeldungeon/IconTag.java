package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.Image;

/**
 * A toolbar tag with an icon and an action, placed with a [TagPlacement].
 */
class IconTag extends Tag {

	private static final float DISABLED_ALPHA = 0.35f;

	private final Image icon;
	private final Runnable action;
	private boolean usable = true;

	IconTag(int color, Image icon, Runnable action) {
		super(color);
		this.icon = icon;
		this.action = action;
		add(icon);
		setSize(Tag.SIZE, Tag.SIZE);
	}

	void usable(boolean value) {
		if (usable == value) return;
		usable = value;
		icon.alpha(value ? 1f : DISABLED_ALPHA);
	}

	@Override
	public void onClick() {
		if (usable) action.run();
	}

	@Override
	public void layout() {
		super.layout();
		if (icon == null) return;
		icon.x = left() + (width() - icon.width()) / 2f + (flipped ? -1 : 1);
		icon.y = top() + (height() - icon.height()) / 2f;
		PixelScene.align(icon);
	}
}
