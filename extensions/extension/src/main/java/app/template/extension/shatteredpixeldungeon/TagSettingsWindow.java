package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;

import java.util.List;

/**
 * Position, colour and icon settings for a toolbar tag. Any tag that uses a [TagPlacement] can open this page.
 */
public class TagSettingsWindow extends EditorWindow {

	private static final String INFO = "Tag style\n\nPosition: pick the screen edge, then how many pixels along it the tag sits, from the top of the left and right edges or from the left end of the top edge. Tap the number to type it.\n\nColor: the tag's colour. Tap a swatch, or set Hue, Saturation and Lightness. Reset goes back to the tag's own colour.\n\nIcon: every icon in the game, in a list you can scroll. The buttons above it show only one kind: interface icons, item sprites, small item icons, status icons or talent icons. Tap one to use it, and turn it in steps of 15 degrees.\n\nIcon Color: tint the icon, or Original for its own colours.\n\nSize: the tag's width and height on screen in pixels (the game's tags are 24 by 24), and the icon's size as a percentage. Reset puts the tag back to 24 by 24.\n\nOpacity: how see-through the tag and its icon are, from 0% (invisible) to 100% (solid). With Link tag and icon ticked, changing one changes both.\n\nOn the top edge the tag turns to face the top of the screen, and on the left edge it is mirrored, so it always looks attached to its edge.";
	private static final String[] TABS_TOP = { "Position", "Size", "Color" };
	private static final String[] TABS_BOTTOM = { "Icon", "Icon Color", "Opacity" };
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
	private static int category;
	private static float iconScroll;
	private static final int ICON_ROWS_SHOWN = 5;

	private ScrollPane iconPane;
	private float iconContentHeight;
	private float viewHeight;

	private final TagPlacement placement;
	private final String title;
	private final Runnable back;
	private final Runnable onDelete;

	public TagSettingsWindow(TagPlacement placement, String title, Runnable back) {
		this(placement, title, back, null);
	}

	/**
	 * With [onDelete], the page also has a Delete Tag button that asks first.
	 */
	public TagSettingsWindow(TagPlacement placement, String title, Runnable back, Runnable onDelete) {
		super();
		this.placement = placement;
		this.title = title;
		this.back = back;
		this.onDelete = onDelete;
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
		if (tab == 1) addSizes();
		if (tab == 2) addColorChooser(placement.color(), placement::color, placement::resetColor, false);
		if (tab == 3) addIconChooser();
		if (tab == 4) addColorChooser(placement.iconColor(), placement::iconColor, placement::resetIconColor, true);
		if (tab == 5) addOpacity();

		pos += GAP;
		if (onDelete != null) {
			addButton("Delete Tag", 0, WIDTH, () -> ConfirmPrompt.ask("Delete Tag", "Delete this tag?", "Delete", () -> {
				hide();
				onDelete.run();
				back.run();
			})).icon(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.CLOSE));
			pos += ROW_HEIGHT + GAP;
		}
		addButton("Back", 0, WIDTH, () -> {
			hide();
			back.run();
		});
		pos += ROW_HEIGHT;
		finish();
		relayoutIcons();
	}

	private void addPreview() {
		IconTag preview = new IconTag(() -> { });
		int width = placement.tagWidth();
		int height = placement.tagHeight();
		preview.setRect((WIDTH - width) / 2f, pos, width, height);
		boolean auto = placement.isAuto();
		preview.flip(!auto && placement.side() == TagPlacement.Side.LEFT);
		preview.onTop(!auto && placement.side() == TagPlacement.Side.TOP);
		preview.style(placement);
		add(preview);
		pos += height + 2 * GAP;
	}

	private void addColorChooser(int current, ColorSetter setter, Runnable reset, boolean original) {
		float width = (WIDTH - (PER_ROW - 1) * GAP) / (float) PER_ROW;
		int count = SWATCHES.length + (original ? 1 : 0);
		for (int i = 0; i < count; i++) {
			boolean isOriginal = original && i == 0;
			int color = isOriginal ? -1 : SWATCHES[original ? i - 1 : i];
			if (!original && i == 0) color = placement.defaultColor();
			int swatch = color;
			int column = i % PER_ROW;
			if (i > 0 && column == 0) pos += SWATCH_HEIGHT + GAP;
			float x = column * (width + GAP);
			boolean selected = color == current;
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
					if (swatch < 0 || (!original && swatch == placement.defaultColor())) {
						reset.run();
					} else {
						setter.set(swatch);
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
		String[] first = { IconCatalog.CATEGORIES[0], IconCatalog.CATEGORIES[1], IconCatalog.CATEGORIES[2] };
		String[] second = { IconCatalog.CATEGORIES[3], IconCatalog.CATEGORIES[4], IconCatalog.CATEGORIES[5] };
		addButton("Default Icon", 0, WIDTH, () -> {
			placement.resetIcon();
			refresh();
		});
		pos += ROW_HEIGHT + GAP;
		addButtonRow(first, category, index -> chooseCategory(index));
		addButtonRow(second, category - first.length, index -> chooseCategory(first.length + index));

		List<String> tokens = IconCatalog.tokens(category);
		String current = placement.icon();
		float cellWidth = (WIDTH - (PER_ROW - 1) * GAP) / (float) PER_ROW;
		Component content = new Component();
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			float x = (i % PER_ROW) * (cellWidth + GAP);
			float y = (i / PER_ROW) * (ICON_CELL + GAP);
			if (token.equals(current)) {
				ColorBlock outline = new ColorBlock(cellWidth, ICON_CELL, SELECTED_COLOR);
				outline.x = x;
				outline.y = y;
				content.add(outline);
				ColorBlock inner = new ColorBlock(cellWidth - 2, ICON_CELL - 2, 0xFF3A3A3A);
				inner.x = x + 1;
				inner.y = y + 1;
				content.add(inner);
			}
			Image icon = IconTag.iconImage(token);
			float scale = Math.min(1f, Math.min((cellWidth - 2) / icon.width(), (ICON_CELL - 2) / icon.height()));
			icon.scale.set(scale, scale);
			icon.x = x + (cellWidth - icon.width()) / 2f;
			icon.y = y + (ICON_CELL - icon.height()) / 2f;
			content.add(icon);
		}
		int rows = (tokens.size() + PER_ROW - 1) / PER_ROW;
		float contentHeight = rows * (ICON_CELL + GAP);
		content.setSize(WIDTH, contentHeight);
		ScrollPane pane = new ScrollPane(content) {
			@Override
			public void onClick(float x, float y) {
				int column = (int) (x / (cellWidth + GAP));
				int row = (int) (y / (ICON_CELL + GAP));
				int index = row * PER_ROW + column;
				if (column < 0 || column >= PER_ROW || index < 0 || index >= tokens.size()) return;
				placement.icon(tokens.get(index));
				iconScroll = content.camera == null ? 0 : content.camera.scroll.y;
				refresh();
			}
		};
		add(pane);
		viewHeight = Math.min(contentHeight, ICON_ROWS_SHOWN * (ICON_CELL + GAP));
		pane.setRect(0, pos, WIDTH, viewHeight);
		iconPane = pane;
		iconContentHeight = contentHeight;
		pos += viewHeight + 2 * GAP;

		int rotation = placement.iconRotation();
		addStepper("Rotation", rotation + " deg", rotation, 15, 0, 345, value -> placement.iconRotation(value), true, placement::resetIconRotation);
	}

	private void chooseCategory(int index) {
		if (index != category) iconScroll = 0;
		category = index;
		refresh();
	}

	/**
	 * The scroll area takes its screen position when laid out, so it is laid out again once the window has its
	 * final size and place, then scrolled back to where it was.
	 */
	private void relayoutIcons() {
		if (iconPane == null) return;
		iconPane.setRect(iconPane.left(), iconPane.top(), iconPane.width(), iconPane.height());
		float maxScroll = Math.max(0, iconContentHeight - viewHeight);
		iconPane.scrollTo(0, Math.max(0, Math.min(maxScroll, iconScroll)));
	}

	private void addSizes() {
		int width = placement.tagWidth();
		int height = placement.tagHeight();
		int scale = placement.iconScale();
		addStepper("Tag width", width + " px", width, 1, TagPlacement.MIN_SIZE, TagPlacement.MAX_SIZE, placement::tagWidth, true, placement::resetSize);
		addStepper("Tag height", height + " px", height, 1, TagPlacement.MIN_SIZE, TagPlacement.MAX_SIZE, placement::tagHeight, true, placement::resetSize);
		addStepper("Icon scale", scale + "%", scale, 25, TagPlacement.MIN_ICON_SCALE, TagPlacement.MAX_ICON_SCALE, placement::iconScale, true, placement::resetIconScale);
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
		show(new TagSettingsWindow(placement, title, back, onDelete));
	}
}
