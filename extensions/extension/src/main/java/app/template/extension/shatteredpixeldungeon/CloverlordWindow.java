package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class CloverlordWindow extends Window {

	private static final int WIDTH = 120;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;
	private static final int TITLE_COLOR = 0xFFFF44;

	private float pos;

	public CloverlordWindow() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock("Extras", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, GAP);
		add(title);
		pos = title.bottom() + 2 * GAP;

		if (UnlockAll.isAvailable()) {
			addLabel("Unlocks");
			addToggle("All heroes", UnlockAll.HEROES);
			addToggle("Guide books", UnlockAll.GUIDES);
			addToggle("Intros and lore", UnlockAll.LORE);
			addToggle("Item catalog", UnlockAll.ITEMS);
			addToggle("Bestiary", UnlockAll.BESTIARY);
		}

		if (SaveBackupActivity.isAvailable()) {
			addLabel("Saves");
			addButton("Export saves", Icons.DATA, () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_EXPORT));
			addButton("Import saves", Icons.CHANGES, () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_IMPORT));
		}

		resize(WIDTH, (int) pos);
	}

	private void addLabel(String text) {
		RenderedTextBlock label = PixelScene.renderTextBlock(text, 7);
		label.hardlight(TITLE_COLOR);
		label.setPos(0, pos + GAP);
		add(label);
		pos = label.bottom() + GAP;
	}

	private void addToggle(String text, String key) {
		CheckBox box = new CheckBox(text) {
			@Override
			public void onClick() {
				super.onClick();
				CloverlordSettings.set(key, checked());
			}
		};
		box.checked(CloverlordSettings.get(key));
		box.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(box);
		pos = box.bottom() + GAP;
	}

	private void addButton(String text, Icons icon, Runnable action) {
		RedButton button = new RedButton(text) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		button.icon(Icons.get(icon));
		button.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(button);
		pos = button.bottom() + GAP;
	}
}
