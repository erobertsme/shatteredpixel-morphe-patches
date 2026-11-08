package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.Image;

/**
 * A toolbar tag in the game's own style, with an icon and an action. Its colour, icon, icon colour and icon
 * rotation come from a [TagPlacement].
 */
class IconTag extends Tag {

	private final Runnable action;
	private Image icon;
	private String iconName;
	private int color = -1;
	private int iconColor = -2;
	private int rotation = -1;
	private boolean onTop;
	private float tagAlpha = -1;
	private float iconAlpha = -1;

	IconTag(Runnable action) {
		super(TagPlacement.DEFAULT_COLOR);
		this.action = action;
		setSize(Tag.SIZE, Tag.SIZE);
	}

	/**
	 * Builds the icon for [name]: an Icons name, or ITEM:id or BUFF:id for item and status icons.
	 */
	static Image iconImage(String name) {
		Image image = StatIcons.fromToken(name);
		if (image != null) return image;
		try {
			return Icons.get(Icons.valueOf(name));
		} catch (Throwable e) {
			return Icons.get(Icons.ARROW);
		}
	}

	static void tintIcon(Image image, int color) {
		if (color < 0) {
			image.resetColor();
			return;
		}
		image.hardlight(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f);
	}

	/**
	 * Turns the tag's base to face the top edge. The base is drawn for the right edge, and flip() mirrors it for
	 * the left edge.
	 */
	void onTop(boolean value) {
		if (onTop == value) return;
		onTop = value;
		layout();
	}

	void style(TagPlacement placement) {
		int newColor = placement.color();
		if (newColor != color) {
			color = newColor;
			setColor(newColor);
		}
		String name = placement.icon();
		if (!name.equals(iconName)) {
			if (icon != null) {
				icon.killAndErase();
				icon.destroy();
			}
			iconName = name;
			icon = iconImage(name);
			add(icon);
			iconColor = -2;
			rotation = -1;
			iconAlpha = -1;
		}
		int newIconColor = placement.iconColor();
		if (newIconColor != iconColor) {
			iconColor = newIconColor;
			tintIcon(icon, newIconColor);
		}
		int newRotation = placement.iconRotation();
		if (newRotation != rotation) {
			rotation = newRotation;
			icon.origin.set(icon.width() / 2f, icon.height() / 2f);
			icon.angle = newRotation;
		}
		float newTagAlpha = placement.tagOpacity() / 100f;
		if (newTagAlpha != tagAlpha && bg != null) {
			tagAlpha = newTagAlpha;
			bg.alpha(newTagAlpha);
		}
		float newIconAlpha = placement.iconOpacity() / 100f;
		if (newIconAlpha != iconAlpha) {
			iconAlpha = newIconAlpha;
			icon.alpha(newIconAlpha);
		}
		layout();
	}

	@Override
	public void onClick() {
		action.run();
	}

	@Override
	public void layout() {
		super.layout();
		if (bg != null) {
			bg.origin.set(width() / 2f, height() / 2f);
			bg.angle = onTop ? -90 : 0;
		}
		if (icon == null) return;
		if (onTop) {
			icon.x = left() + (width() - icon.width()) / 2f;
			icon.y = top() + (height() - icon.height()) / 2f - 1;
		} else {
			if (!flipped) icon.x = left() + (Tag.SIZE - icon.width()) / 2f + 1;
			else icon.x = left() + width() - (Tag.SIZE + icon.width()) / 2f - 1;
			icon.y = top() + (height() - icon.height()) / 2f;
		}
		PixelScene.align(icon);
	}
}
