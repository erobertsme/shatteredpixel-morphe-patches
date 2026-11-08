package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;

/**
 * Position and colour settings for a group of toolbar tags. Any tag that uses a [TagPlacement] can open this page.
 */
public class TagSettingsWindow extends EditorWindow {

	private static final String INFO = "Tags\n\nPosition: pick the screen edge, then how many pixels along it the tags start, from the top of the left and right edges or from the left end of the top edge. Tap the number to type it.\n\nColor: tap a swatch, or set Hue, Saturation and Lightness. The reset buttons go back to the toolbar's grey.";
	private static final int[] SWATCHES = {
		TagPlacement.DEFAULT_COLOR, 0xC0392B, 0xE67E22, 0xD4B92A, 0x3E9B4F, 0x2A9D8F,
		0x3A6EC8, 0x8E44AD, 0xD9579A, 0x8B5A2B, 0xE0E0E0, 0x303030,
	};
	private static final int PER_ROW = 6;
	private static final int SWATCH_HEIGHT = 14;
	private static final int SELECTED_COLOR = 0xFFFFFF44;

	private final TagPlacement placement;
	private final String title;
	private final Runnable back;

	public TagSettingsWindow(TagPlacement placement, String title, Runnable back) {
		super();
		this.placement = placement;
		this.title = title;
		this.back = back;
		addTitle(title, INFO);

		addSection("Position");
		addPlacementSettings(placement);

		addSection("Color");
		addPreview();
		addSwatches();
		addHslSteppers();

		pos += GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			back.run();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	private void addPreview() {
		IconTag preview = new IconTag(StatIcons.arrow(false), () -> { });
		preview.color(placement.color());
		preview.setRect((WIDTH - IconTag.WIDTH) / 2f, pos, IconTag.WIDTH, IconTag.HEIGHT);
		add(preview);
		pos += IconTag.HEIGHT + GAP;
	}

	private void addSwatches() {
		int current = placement.color();
		float width = (WIDTH - (PER_ROW - 1) * GAP) / (float) PER_ROW;
		for (int i = 0; i < SWATCHES.length; i++) {
			int color = SWATCHES[i];
			int column = i % PER_ROW;
			if (i > 0 && column == 0) pos += SWATCH_HEIGHT + GAP;
			float x = column * (width + GAP);
			if (color == current) {
				ColorBlock outline = new ColorBlock(width, SWATCH_HEIGHT, SELECTED_COLOR);
				outline.x = x;
				outline.y = pos;
				add(outline);
			}
			ColorBlock block = new ColorBlock(width - 2, SWATCH_HEIGHT - 2, 0xFF000000 | color);
			block.x = x + 1;
			block.y = pos + 1;
			add(block);
			Button button = new Button() {
				@Override
				public void onClick() {
					if (color == TagPlacement.DEFAULT_COLOR) {
						placement.resetColor();
					} else {
						placement.color(color);
					}
					refresh();
				}
			};
			button.setRect(x, pos, width, SWATCH_HEIGHT);
			add(button);
		}
		pos += SWATCH_HEIGHT + 2 * GAP;
	}

	private void addHslSteppers() {
		float[] hsl = Colors.toHsl(placement.color());
		int hue = Math.round(hsl[0]);
		int saturation = Math.round(hsl[1] * 100);
		int lightness = Math.round(hsl[2] * 100);
		addStepper("Hue", String.valueOf(hue), hue, 10, 0, 359, value -> setHsl(((value % 360) + 360) % 360, saturation, lightness), true, placement::resetColor);
		addStepper("Saturation", saturation + "%", saturation, 5, 0, 100, value -> setHsl(hue, value, lightness), true, placement::resetColor);
		addStepper("Lightness", lightness + "%", lightness, 5, 0, 100, value -> setHsl(hue, saturation, value), true, placement::resetColor);
	}

	private void setHsl(int hue, int saturation, int lightness) {
		int s = Math.max(0, Math.min(100, saturation));
		int l = Math.max(0, Math.min(100, lightness));
		placement.color(Colors.fromHsl(hue, s / 100f, l / 100f));
	}

	@Override
	protected void reopen() {
		show(new TagSettingsWindow(placement, title, back));
	}
}
