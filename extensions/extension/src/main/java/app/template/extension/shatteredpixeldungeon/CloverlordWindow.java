package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

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
		pos = title.bottom() + GAP;

		RenderedTextBlock version = PixelScene.renderTextBlock("Cloverlord Patches " + CloverlordMenu.patchesVersion(), 6);
		version.hardlight(0x999999);
		version.setPos((WIDTH - version.width()) / 2f, pos);
		add(version);
		pos = version.bottom() + 2 * GAP;

		if (HungerEditor.isAvailable()) {
			addLabel("Gameplay");
			addButton("Hunger", new ItemSprite(new Food()), () -> {
				hide();
				HungerWindow.open(CloverlordMenu::show);
			});
		}

		if (SupporterRewards.isAvailable()) {
			addLabel("Supporter options");
			addToggle("Hide Support button", CloverlordMenu.HIDE_SUPPORT);
			addTierButtons();
		}

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
			addButton("Export saves", new TalentIcon(Talent.ENHANCED_RINGS), () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_EXPORT));
			addButton("Import saves", Icons.CHANGES, () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_IMPORT));
		}

		resize(WIDTH, (int) pos);
	}

	private void addTierButtons() {
		String[] names = SupporterRewards.TIER_NAMES;
		float width = (WIDTH - (names.length - 1) * GAP) / (float) names.length;
		int selected = SupporterRewards.tier();
		for (int i = 0; i < names.length; i++) {
			final int tier = i;
			RedButton button = new RedButton(names[i], 6) {
				@Override
				public void onClick() {
					SupporterRewards.tier(tier);
					hide();
					CloverlordMenu.show();
				}
			};
			if (i == selected) button.textColor(TITLE_COLOR);
			button.setRect(i * (width + GAP), pos, width, BTN_HEIGHT);
			add(button);
		}
		pos += BTN_HEIGHT + GAP;
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
		addButton(text, Icons.get(icon), action);
	}

	private void addButton(String text, Image icon, Runnable action) {
		RedButton button = new RedButton(text) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		button.icon(icon);
		button.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(button);
		pos = button.bottom() + GAP;
	}
}
