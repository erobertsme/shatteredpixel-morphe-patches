package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;

/**
 * Position, colour and icon settings for a toolbar tag. Any tag that uses a [TagPlacement] can open this page.
 */
public class TagSettingsWindow extends EditorWindow {

	private static final String INFO = "Tag style\n\nPosition: pick the screen edge, then how many pixels along it the tag sits, from the top of the left and right edges or from the left end of the top edge. Tap the number to type it.\n\nColor: the tag's colour. Tap a swatch, or set Hue, Saturation and Lightness. Reset goes back to the game's grey.\n\nIcon: pick the icon and turn it in steps of 15 degrees.\n\nIcon Color: tint the icon, or Original for its own colours.\n\nOpacity: how see-through the tag and its icon are, from 0% (invisible) to 100% (solid). With Link tag and icon ticked, changing one changes both.\n\nOn the top edge the tag turns to face the top of the screen, and on the left edge it is mirrored, so it always looks attached to its edge.";
	private static final String[] TABS_TOP = { "Position", "Color", "Icon" };
	private static final String[] TABS_BOTTOM = { "Icon Color", "Opacity" };
	private static final int[] SWATCHES = {
		TagPlacement.DEFAULT_COLOR, 0xC0392B, 0xE67E22, 0xD4B92A, 0x3E9B4F, 0x2A9D8F,
		0x3A6EC8, 0x8E44AD, 0xD9579A, 0x8B5A2B, 0xE0E0E0, 0x303030,
	};
	private static final int PER_ROW = 6;
	private static final int SWATCH_HEIGHT = 14;
	private static final int ICON_CELL = 18;
	private static final int SELECTED_COLOR = 0xFFFFFF44;

	interface ColorSetter {
		void set(int rgb);
	}

	private static int tab;

	private final TagPlacement placement;
	private final String title;
	private final Runnable back;

	public TagSettingsWindow(TagPlacement placement, String title, Runnable back) {
		super();
		this.placement = placement;
		this.title = title;
		this.back = back;
		addTitle(title, INFO);
		addPreview();
		addButtonRow(TABS_TOP, tab, index -> {
			tab = index;
			refresh();
		});
		addButtonRow(TABS_BOTTOM, tab - TABS_TOP.length, index -> {
			tab = TABS_TOP.length + index;
			refresh();
		});
		pos += GAP;
		if (tab == 0) addPlacementSettings(placement);
		if (tab == 1) addColorChooser(placement.color(), placement::color, placement::resetColor, false);
		if (tab == 2) addIconChooser();
		if (tab == 3) addColorChooser(placement.iconColor(), placement::iconColor, placement::resetIconColor, true);
		if (tab == 4) addOpacity();

		pos += GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			back.run();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private void addPreview() {
		IconTag preview = new IconTag(() -> { });
		preview.setRect((WIDTH - Tag.SIZE) / 2f, pos, Tag.SIZE, Tag.SIZE);
		preview.flip(placement.side() == TagPlacement.Side.LEFT);
		preview.onTop(placement.side() == TagPlacement.Side.TOP);
		preview.style(placement);
		add(preview);
		pos += Tag.SIZE + 2 * GAP;
	}

	private void addColorChooser(int current, ColorSetter setter, Runnable reset, boolean original) {
		float width = (WIDTH - (PER_ROW - 1) * GAP) / (float) PER_ROW;
		int count = SWATCHES.length + (original ? 1 : 0);
		for (int i = 0; i < count; i++) {
			boolean isOriginal = original && i == 0;
			int color = isOriginal ? -1 : SWATCHES[original ? i - 1 : i];
			int column = i % PER_ROW;
			if (i > 0 && column == 0) pos += SWATCH_HEIGHT + GAP;
			float x = column * (width + GAP);
			boolean selected = color == current || (!original && color == TagPlacement.DEFAULT_COLOR && current == TagPlacement.DEFAULT_COLOR);
			if (selected) {
				ColorBlock outline = new ColorBlock(width, SWATCH_HEIGHT, SELECTED_COLOR);
				outline.x = x;
				outline.y = pos;
				add(outline);
			}
			if (isOriginal) {
				Image icon = IconTag.iconImage(placement.icon());
				icon.x = x + (width - icon.width()) / 2f;
				icon.y = pos + (SWATCH_HEIGHT - icon.height()) / 2f;
				add(icon);
			} else {
				ColorBlock block = new ColorBlock(width - 2, SWATCH_HEIGHT - 2, 0xFF000000 | color);
				block.x = x + 1;
				block.y = pos + 1;
				add(block);
			}
			Button button = new Button() {
				@Override
				public void onClick() {
					if (color < 0 || (!original && color == TagPlacement.DEFAULT_COLOR)) {
						reset.run();
					} else {
						setter.set(color);
					}
					refresh();
				}
			};
			button.setRect(x, pos, width, SWATCH_HEIGHT);
			add(button);
		}
		pos += SWATCH_HEIGHT + 2 * GAP;

		int base = current < 0 ? 0xFFFFFF : current;
		float[] hsl = Colors.toHsl(base);
		int hue = Math.round(hsl[0]);
		int saturation = Math.round(hsl[1] * 100);
		int lightness = Math.round(hsl[2] * 100);
		addStepper("Hue", String.valueOf(hue), hue, 10, 0, 359, value -> setter.set(Colors.fromHsl(((value % 360) + 360) % 360, saturation / 100f, lightness / 100f)), true, reset);
		addStepper("Saturation", saturation + "%", saturation, 5, 0, 100, value -> setter.set(Colors.fromHsl(hue, clamp(value) / 100f, lightness / 100f)), true, reset);
		addStepper("Lightness", lightness + "%", lightness, 5, 0, 100, value -> setter.set(Colors.fromHsl(hue, saturation / 100f, clamp(value) / 100f)), true, reset);
	}

	private void addIconChooser() {
		String current = placement.icon();
		float width = (WIDTH - (PER_ROW - 1) * GAP) / (float) PER_ROW;
		for (int i = 0; i < TagPlacement.ICONS.length; i++) {
			String name = TagPlacement.ICONS[i];
			int column = i % PER_ROW;
			if (i > 0 && column == 0) pos += ICON_CELL + GAP;
			float x = column * (width + GAP);
			if (name.equals(current)) {
				ColorBlock outline = new ColorBlock(width, ICON_CELL, SELECTED_COLOR);
				outline.x = x;
				outline.y = pos;
				add(outline);
				ColorBlock inner = new ColorBlock(width - 2, ICON_CELL - 2, 0xFF3A3A3A);
				inner.x = x + 1;
				inner.y = pos + 1;
				add(inner);
			}
			Image icon = IconTag.iconImage(name);
			float scale = Math.min(1f, Math.min((width - 2) / icon.width(), (ICON_CELL - 2) / icon.height()));
			icon.scale.set(scale, scale);
			icon.x = x + (width - icon.width()) / 2f;
			icon.y = pos + (ICON_CELL - icon.height()) / 2f;
			add(icon);
			Button button = new Button() {
				@Override
				public void onClick() {
					placement.icon(name);
					refresh();
				}
			};
			button.setRect(x, pos, width, ICON_CELL);
			add(button);
		}
		pos += ICON_CELL + 2 * GAP;
		int rotation = placement.iconRotation();
		addStepper("Rotation", rotation + " deg", rotation, 15, 0, 345, value -> placement.iconRotation(value), true, placement::resetIconRotation);
	}

	private void addOpacity() {
		addCheckBox("Link tag and icon", placement.opacityLinked(), () -> placement.opacityLinked(!placement.opacityLinked()));
		int tagOpacity = placement.tagOpacity();
		int iconOpacity = placement.iconOpacity();
		addStepper("Tag", tagOpacity + "%", tagOpacity, 5, 0, 100, placement::tagOpacity, true, placement::resetOpacity);
		addStepper("Icon", iconOpacity + "%", iconOpacity, 5, 0, 100, placement::iconOpacity, true, placement::resetOpacity);
	}

	private static int clamp(int value) {
		return Math.max(0, Math.min(100, value));
	}

	@Override
	protected void reopen() {
		show(new TagSettingsWindow(placement, title, back));
	}
}
