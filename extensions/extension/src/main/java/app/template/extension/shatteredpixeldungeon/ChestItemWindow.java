package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;

/**
 * An item's details from the Quantum Chest, with a button to take it out or store it.
 */
public class ChestItemWindow extends EditorWindow {

	private static final int ICON_SPACE = 20;

	private final Item item;
	private final String actionLabel;
	private final Runnable action;
	private final Runnable back;

	public ChestItemWindow(Item item, String actionLabel, Runnable action, Runnable back) {
		super();
		this.item = item;
		this.actionLabel = actionLabel;
		this.action = action;
		this.back = back;

		ItemSprite icon = new ItemSprite(item);
		icon.x = (ICON_SPACE - icon.width()) / 2f;
		icon.y = pos;
		add(icon);
		RenderedTextBlock title = PixelScene.renderTextBlock(InventoryEditor.capitalize(item.title()), 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.maxWidth(WIDTH - ICON_SPACE);
		title.setPos(ICON_SPACE, pos + (icon.height() - title.height()) / 2f);
		add(title);
		pos = Math.max(icon.y + icon.height(), title.bottom()) + 2 * GAP;

		String info;
		try {
			info = item.info();
		} catch (Throwable e) {
			info = "";
		}
		RenderedTextBlock text = PixelScene.renderTextBlock(info, 6);
		text.maxWidth(WIDTH);
		text.setPos(0, pos);
		add(text);
		pos = text.bottom() + 2 * GAP;

		float half = (WIDTH - GAP) / 2f;
		addButton("Back", 0, half, () -> {
			hide();
			back.run();
		});
		addButton(actionLabel, half + GAP, half, () -> {
			hide();
			action.run();
			back.run();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	public void onBackPressed() {
		hide();
		back.run();
	}

	@Override
	protected void reopen() {
		show(new ChestItemWindow(item, actionLabel, action, back));
	}
}
