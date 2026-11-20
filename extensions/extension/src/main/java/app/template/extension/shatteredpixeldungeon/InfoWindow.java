package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the text behind an [i] button. Paragraphs are separated by a blank line. A short first paragraph becomes the title, a line
 * starting with "## " at the start of a paragraph is a section heading for the lines below it, _text between underscores_ is highlighted, and a paragraph that starts with {NAME} gets that
 * icon beside it: an Icons name, an ITEM: or BUFF: token, or HEART, FOOD or STAR. Long text is split into pages, starting a new page at a
 * section heading when the whole section doesn't fit, and never leaving a heading alone at the bottom of a page.
 */
final class InfoWindow extends Window {

	private static final int WIDTH = 120;
	private static final int GAP = 4;
	private static final int ICON_SPACE = 18;
	private static final int PAGER_HEIGHT = 16;
	private static final int ARROW_WIDTH = 30;
	private static final int HEADING_MAX = 32;
	private static final int MIN_PAGE_HEIGHT = 80;
	private static final int HEART_ICON = 21;
	private static final int TEXT_SIZE = 6;
	private static final int SECTION_SIZE = 7;
	private static final String SECTION_MARK = "## ";

	private static final class Paragraph {
		final String icon;
		final String text;
		final boolean section;

		Paragraph(String icon, String text, boolean section) {
			this.icon = icon;
			this.text = text;
			this.section = section;
		}
	}

	private static final class Content {
		String heading;
		final List<Paragraph> paragraphs = new ArrayList<>();
		final List<int[]> pages = new ArrayList<>();
		float pageHeight;
	}

	private final Content content;
	private final int page;

	static void show(String text) {
		Game.scene().addToFront(new InfoWindow(paginate(parse(text)), 0));
	}

	private InfoWindow(Content content, int page) {
		super();
		this.content = content;
		this.page = page;

		float y = 0;
		if (content.heading != null) {
			RenderedTextBlock heading = PixelScene.renderTextBlock(content.heading, 9);
			heading.hardlight(CharacterWindow.TITLE_COLOR);
			heading.maxWidth(WIDTH);
			heading.setPos(0, y);
			add(heading);
			y = heading.bottom() + 2 * GAP;
		}

		int[] range = content.pages.get(page);
		float top = y;
		for (int i = range[0]; i < range[1]; i++) {
			Paragraph paragraph = content.paragraphs.get(i);
			if (paragraph.section && i > range[0]) y += GAP;
			y = addParagraph(paragraph, y) + GAP;
		}
		y = Math.max(y, top + content.pageHeight);

		if (content.pages.size() > 1) y = addPager(y);
		resize(WIDTH, (int) y);
	}

	private float addParagraph(Paragraph paragraph, float y) {
		if (paragraph.section) {
			RenderedTextBlock heading = PixelScene.renderTextBlock(paragraph.text, SECTION_SIZE);
			heading.hardlight(CharacterWindow.TITLE_COLOR);
			heading.maxWidth(WIDTH);
			heading.setPos(0, y);
			add(heading);
			return heading.bottom();
		}
		float left = 0;
		Image icon = paragraph.icon == null ? null : icon(paragraph.icon);
		if (icon != null) {
			icon.x = (ICON_SPACE - 2 - icon.width()) / 2f;
			icon.y = y;
			add(icon);
			left = ICON_SPACE;
		}
		RenderedTextBlock text = PixelScene.renderTextBlock(paragraph.text, TEXT_SIZE);
		text.maxWidth((int) (WIDTH - left));
		text.setPos(left, y);
		add(text);
		float bottom = text.bottom();
		if (icon != null) bottom = Math.max(bottom, icon.y + icon.height());
		return bottom;
	}

	private float addPager(float y) {
		int count = content.pages.size();
		RedButton previous = new RedButton("", 7) {
			@Override
			public void onClick() {
				turn((page - 1 + count) % count);
			}
		};
		previous.icon(StatIcons.arrow(true));
		previous.setRect(0, y, ARROW_WIDTH, PAGER_HEIGHT);
		add(previous);

		RedButton next = new RedButton("", 7) {
			@Override
			public void onClick() {
				turn((page + 1) % count);
			}
		};
		next.icon(StatIcons.arrow(false));
		next.setRect(WIDTH - ARROW_WIDTH, y, ARROW_WIDTH, PAGER_HEIGHT);
		add(next);

		RenderedTextBlock label = PixelScene.renderTextBlock((page + 1) + "/" + count, 8);
		label.setPos((WIDTH - label.width()) / 2f, y + (PAGER_HEIGHT - label.height()) / 2f);
		add(label);
		return y + PAGER_HEIGHT;
	}

	private void turn(int target) {
		hide();
		Game.scene().addToFront(new InfoWindow(content, target));
	}

	private static Content parse(String text) {
		Content content = new Content();
		String[] parts = text.split("\n\n");
		int start = 0;
		if (parts.length > 1 && isHeading(parts[0])) {
			content.heading = parts[0];
			start = 1;
		}
		for (int i = start; i < parts.length; i++) {
			String part = parts[i].trim();
			if (part.isEmpty()) continue;
			if (part.startsWith(SECTION_MARK)) {
				int lineEnd = part.indexOf('\n');
				String heading = lineEnd < 0 ? part : part.substring(0, lineEnd);
				content.paragraphs.add(new Paragraph(null, heading.substring(SECTION_MARK.length()).trim(), true));
				if (lineEnd < 0) continue;
				part = part.substring(lineEnd + 1).trim();
				if (part.isEmpty()) continue;
			}
			String icon = null;
			if (part.startsWith("{")) {
				int end = part.indexOf('}');
				if (end > 1) {
					icon = part.substring(1, end);
					part = part.substring(end + 1).trim();
				}
			}
			content.paragraphs.add(new Paragraph(icon, part, false));
		}
		return content;
	}

	private static boolean isHeading(String text) {
		return text.length() <= HEADING_MAX && !text.contains(".") && !text.contains("\n") && !text.startsWith("{");
	}

	private static Content paginate(Content content) {
		float maxHeight = Math.max(MIN_PAGE_HEIGHT, PixelScene.uiCamera.height * 0.6f - 40);
		List<Paragraph> paragraphs = content.paragraphs;
		float[] heights = new float[paragraphs.size()];
		for (int i = 0; i < heights.length; i++) heights[i] = measure(paragraphs.get(i)) + GAP + (paragraphs.get(i).section ? GAP : 0);
		int start = 0;
		float height = 0;
		float tallest = 0;
		for (int i = 0; i < paragraphs.size(); i++) {
			boolean breakHere = i > start && startsNewPage(paragraphs, heights, i, height, maxHeight);
			if (breakHere) {
				content.pages.add(new int[] { start, i });
				tallest = Math.max(tallest, height);
				start = i;
				height = 0;
			}
			height += heights[i];
		}
		content.pages.add(new int[] { start, paragraphs.size() });
		tallest = Math.max(tallest, height);
		content.pageHeight = content.pages.size() > 1 ? tallest : 0;
		return content;
	}

	/**
	 * Whether paragraph [index] has to start a new page on a page already [height] tall: when it doesn't fit, or it's a section heading
	 * and either its whole section or the heading with its first paragraph doesn't fit.
	 */
	private static boolean startsNewPage(List<Paragraph> paragraphs, float[] heights, int index, float height, float maxHeight) {
		if (height + heights[index] > maxHeight) return true;
		if (!paragraphs.get(index).section) return false;
		if (height + sectionHeight(paragraphs, heights, index) > maxHeight) return true;
		return index + 1 < heights.length && height + heights[index] + heights[index + 1] > maxHeight;
	}

	/**
	 * The height of the section starting at [start], up to the next section heading.
	 */
	private static float sectionHeight(List<Paragraph> paragraphs, float[] heights, int start) {
		float total = heights[start];
		for (int i = start + 1; i < heights.length && !paragraphs.get(i).section; i++) total += heights[i];
		return total;
	}

	private static float measure(Paragraph paragraph) {
		RenderedTextBlock text = PixelScene.renderTextBlock(paragraph.text, paragraph.section ? SECTION_SIZE : TEXT_SIZE);
		text.maxWidth(paragraph.icon == null ? WIDTH : WIDTH - ICON_SPACE);
		float height = text.height();
		text.destroy();
		return paragraph.icon == null ? height : Math.max(height, 16);
	}

	private static Image icon(String name) {
		try {
			if (name.startsWith("ITEM:") || name.startsWith("BUFF:")) return StatIcons.fromToken(name);
			if (name.equals("HEART")) return new BuffIcon(HEART_ICON, true);
			if (name.equals("STAR")) return Icons.get(Icons.TALENT);
			if (name.equals("FOOD")) return new ItemSprite(new Food());
			return Icons.get(Icons.valueOf(name));
		} catch (Throwable e) {
			return null;
		}
	}
}
