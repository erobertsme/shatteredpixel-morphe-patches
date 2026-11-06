package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

import java.util.ArrayList;
import java.util.List;

public class CloverlordWindow extends Window {

	private static final int WIDTH = 128;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;
	private static final int TITLE_COLOR = 0xFFFF44;
	private static final int UPGRADE_ICON = 50;
	private static final String INFO = "Extras holds the options added by Cloverlord Patches. Tabs and sections only appear when their patch is included.\n\nGeneral: hunger settings and save backups. QoL: quality of life options. Unlocks: unlocks and supporter options. Editors: switch each editor on or off.\n\nThe Editors tab switches each editor on or off. A switched-off editor hides its buttons. Switching off the Hunger editor also puts hunger back to normal until you switch it on again.\n\nUnlocks: shows heroes, guide pages, lore, catalog items and bestiary entries as unlocked without changing your save. Untick to get your real progress back.\n\nSaves: on the main menu, export every save, including game saves, to one zip file or import one. Importing replaces your current saves, keeps a copy of them in the app's cache, and closes the game. In a game, Game Saves keeps up to 10 saves of that game to load back later. When a game ends, the title screen asks whether to delete or keep its saves, and Clean Up Saves deletes any you kept.\n\nSettings you change during a game are kept with that game. The main menu settings are the defaults for new games.\n\n{FOOD}Hunger: change how hunger and starvation work.\n\nList everything on a tile (Editors tab, part of the Floor editor): examining a tile lists every item in a pile, any creature, plant or trap, and the tile itself. Pick an item from a pile to pick up that one instead of the top item.\n\nUnlimited inventory space and the Weapons, Armor and Rings backpack tabs are in the Inventory editor, under Inventory in the in-game menu.\n\nThe Floor editor is in the in-game menu, and on the Edit Tile button when you examine a tile, trap or item on the floor. Examining an item on the floor also lets you move it, or the whole pile, to another tile.\n\nConfirm actions: choose which taps ask first, such as attacking, walking to a distant tile, using stairs or stepping into danger, so a stray tap can't ruin your run.\n\nQuantum Chest (QoL tab): after you beat a boss, a chest appears beside the exit of that floor. Every quantum chest shares the same storage of 25, 50 or 75 items, so you can drop something off on one boss floor and pick it up on another. Tap the chest while next to it to open it.\n\nBackpack (QoL tab): Rows of items sets how many rows each page of the backpack and every bag shows, from the game's 4 up to 8, and each extra row is 5 more slots. Extra page for each tab gives every bag a second page of the same size. With unlimited inventory space on, bags show 8 rows and grow as needed instead.\n\nBackpack tabs (QoL tab): Weapons, Armor and Rings tabs that hold those items like the potion bandolier holds potions, 19 each, so they don't use backpack space. Needs the Inventory editor patch.\n\nExtra quickslot row (QoL tab): six more quickslots in a row above the toolbar. With the game's Quickslot Swapper on, there is no extra row; the swap button cycles through all twelve slots, three at a time, instead. Takes effect the next time the game screen opens, such as on the next floor.\n\nShopkeeper (QoL tab): keep enemies out of the shop, stop the shopkeeper running off, and choose how many sold items can be bought back.\n\nSupporter options: keep your supporter rewards and hide the Support button.";

	private float pos;

	private static final int TAB_GENERAL = 0;
	private static final int TAB_QOL = 1;
	private static final int TAB_UNLOCKS = 2;
	private static final int TAB_EDITORS = 3;
	private static final String[] TAB_NAMES = { "General", "QoL", "Unlocks", "Editors" };

	private static int page;

	public CloverlordWindow() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock("Extras", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, GAP);
		add(title);
		InfoButton.add(this, WIDTH, 0, INFO);
		pos = title.bottom() + GAP;

		RenderedTextBlock version = PixelScene.renderTextBlock("Cloverlord Patches " + CloverlordMenu.patchesVersion(), 6);
		version.hardlight(0x999999);
		version.setPos((WIDTH - version.width()) / 2f, pos);
		add(version);
		pos = version.bottom() + 2 * GAP;

		List<Integer> tabs = availableTabs();
		if (!tabs.contains(page)) page = TAB_GENERAL;
		if (tabs.size() > 1) addTabs(tabs);

		if (page == TAB_EDITORS) buildEditors();
		if (page == TAB_QOL) buildQualityOfLife();
		if (page == TAB_UNLOCKS) buildUnlocks();
		if (page == TAB_GENERAL) buildGeneral();
		pos += 2 * GAP;
		addButton("Back", (Image) null, this::back);
		resize(WIDTH, (int) (pos - GAP));
	}

	private void back() {
		hide();
		if (Game.scene() instanceof GameScene) CloverlordMenu.showGameMenu();
	}

	private static List<Integer> availableTabs() {
		List<Integer> tabs = new ArrayList<>();
		tabs.add(TAB_GENERAL);
		if (QolSettings.isAvailable()) tabs.add(TAB_QOL);
		if (UnlockAll.isAvailable() || SupporterRewards.isAvailable()) tabs.add(TAB_UNLOCKS);
		if (EditorSwitches.anyAvailable()) tabs.add(TAB_EDITORS);
		return tabs;
	}

	private void buildGeneral() {
		if (EditorSwitches.hunger()) {
			addLabel("Gameplay");
			addButton("Hunger", new ItemSprite(new Food()), () -> {
				hide();
				HungerWindow.open(CloverlordMenu::show);
			});
		}

		if (SaveBackupActivity.isAvailable() && GameSettings.inGame()) {
			addLabel("Saves");
			addButton("Game Saves", Icons.SCROLL_COLOR, () -> {
				hide();
				EditorWindow.show(new GameSavesWindow(0));
			});
		} else if (SaveBackupActivity.isAvailable()) {
			addLabel("Saves");
			addButton("Export saves", exportIcon(), () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_EXPORT));
			addButton("Import saves", Icons.CHANGES, () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_IMPORT));
			int finished = GameSaves.finishedFolders().size();
			if (finished > 0) {
				addButton("Clean Up Saves (" + finished + ")", Icons.CLOSE, () -> ConfirmPrompt.ask("Clean Up Saves",
					"Delete the game saves of " + finished + (finished == 1 ? " game that has" : " games that have") + " ended? Saves of games in progress are kept.",
					"Delete", () -> {
						GameSaves.cleanUp();
						hide();
						CloverlordMenu.show();
					}));
			}
		}

		if (!EditorSwitches.hunger() && !SaveBackupActivity.isAvailable()) addLabel("Nothing here yet. Try the other tabs.");
	}

	private void buildQualityOfLife() {
		addLabel("Quality of life");
		addButton("Confirm actions", (Image) null, () -> {
			hide();
			EditorWindow.show(new ConfirmWindow());
		});
		addButton("Shopkeeper", (Image) null, () -> {
			hide();
			EditorWindow.show(new ShopkeeperWindow());
		});
		addLabel("Backpack");
		addBagRowButtons();
		addOption("Extra page for each tab", QolSettings.extraBagPage(), QolSettings::extraBagPage);
		if (InventoryWindow.isAvailable()) {
			for (int kind = 0; kind < FilterBag.count(); kind++) {
				final int tab = kind;
				addOption(FilterBag.label(kind) + " tab", FilterBag.isOn(kind), on -> FilterBag.setOn(tab, on));
			}
		}
		addLabel("Toolbar");
		addOption("Extra quickslot row", QuickslotRow.isOn(), QuickslotRow::setOn);
		addLabel("Quantum Chest");
		addOption("Quantum Chest", QuantumChest.isOn(), QuantumChest::setOn);
		addChestPageButtons();
	}

	private void buildUnlocks() {
		if (UnlockAll.isAvailable()) {
			addLabel("Unlocks");
			addToggle("All heroes", UnlockAll.HEROES);
			addToggle("Guide books", UnlockAll.GUIDES);
			addToggle("Intros and lore", UnlockAll.LORE);
			addToggle("Item catalog", UnlockAll.ITEMS);
			addToggle("Bestiary", UnlockAll.BESTIARY);
		}

		if (SupporterRewards.isAvailable()) {
			addLabel("Supporter options");
			addToggle("Hide Support button", CloverlordMenu.HIDE_SUPPORT);
			addTierButtons();
		}
	}

	private void addTabs(List<Integer> tabs) {
		float width = (WIDTH - (tabs.size() - 1) * GAP) / (float) tabs.size();
		for (int i = 0; i < tabs.size(); i++) {
			int tab = tabs.get(i);
			RedButton button = new RedButton(TAB_NAMES[tab], tabs.size() > 3 ? 6 : 7) {
				@Override
				public void onClick() {
					if (tab == page) return;
					page = tab;
					hide();
					CloverlordMenu.show();
				}
			};
			if (tab == page) button.textColor(TITLE_COLOR);
			button.setRect(i * (width + GAP), pos, width, BTN_HEIGHT);
			add(button);
		}
		pos += BTN_HEIGHT + 2 * GAP;
	}

	private void buildEditors() {
		addLabel("Editors");
		if (CharacterWindow.isAvailable()) addSwitch("Character editor", EditorSwitches.CHARACTER);
		if (InventoryWindow.isAvailable()) addSwitch("Inventory editor", EditorSwitches.INVENTORY);
		if (EnemyWindow.isAvailable()) addSwitch("Enemy and NPC editor", EditorSwitches.ENEMY);
		if (FloorWindow.isAvailable()) addSwitch("Floor editor", EditorSwitches.FLOOR);
		if (HungerEditor.isAvailable()) addSwitch("Hunger editor", EditorSwitches.HUNGER);
		if (Teleport.isAvailable()) addSwitch("Teleport", EditorSwitches.TELEPORT);
		if (FloorWindow.isAvailable()) {
			addLabel("Floor editor");
			addOption("List everything on a tile", TileContents.isOn(), TileContents::setOn);
		}
	}

	private void addSwitch(String text, String key) {
		CheckBox box = new CheckBox(text) {
			@Override
			public void onClick() {
				super.onClick();
				EditorSwitches.setOn(key, checked());
			}
		};
		box.checked(EditorSwitches.isOn(key));
		box.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(box);
		pos = box.bottom() + GAP;
	}

	private static Image exportIcon() {
		BuffIcon icon = new BuffIcon(UPGRADE_ICON, true);
		icon.hardlight(0f, 1f, 0f);
		return icon;
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

	private void addBagRowButtons() {
		RenderedTextBlock label = PixelScene.renderTextBlock("Rows of items", 6);
		label.setPos(0, pos);
		add(label);
		pos = label.bottom() + GAP;
		int selected = QolSettings.bagRows();
		int count = QolSettings.MAX_BAG_ROWS - QolSettings.DEFAULT_BAG_ROWS + 1;
		float width = (WIDTH - (count - 1) * GAP) / (float) count;
		for (int i = 0; i < count; i++) {
			int rows = QolSettings.DEFAULT_BAG_ROWS + i;
			RedButton button = new RedButton(rows == QolSettings.DEFAULT_BAG_ROWS ? "Default" : String.valueOf(rows), 6) {
				@Override
				public void onClick() {
					QolSettings.bagRows(rows);
					hide();
					CloverlordMenu.show();
				}
			};
			if (rows == selected) button.textColor(TITLE_COLOR);
			button.setRect(i * (width + GAP), pos, width, BTN_HEIGHT);
			add(button);
		}
		pos += BTN_HEIGHT + GAP;
	}

	private void addChestPageButtons() {
		int selected = QuantumChest.pages();
		float width = (WIDTH - (QuantumChest.MAX_PAGES - 1) * GAP) / (float) QuantumChest.MAX_PAGES;
		for (int i = 1; i <= QuantumChest.MAX_PAGES; i++) {
			int pages = i;
			RedButton button = new RedButton(pages + (pages == 1 ? " Page" : " Pages"), 6) {
				@Override
				public void onClick() {
					QuantumChest.pages(pages);
					hide();
					CloverlordMenu.show();
				}
			};
			if (i == selected) button.textColor(TITLE_COLOR);
			button.setRect((i - 1) * (width + GAP), pos, width, BTN_HEIGHT);
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

	interface OptionSetter {
		void set(boolean on);
	}

	private void addOption(String text, boolean on, OptionSetter setter) {
		CheckBox box = new CheckBox(text) {
			@Override
			public void onClick() {
				super.onClick();
				setter.set(checked());
			}
		};
		box.checked(on);
		box.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(box);
		pos = box.bottom() + GAP;
	}

	private void addButton(String text, Icons icon, Runnable action) {
		addButton(text, Icons.get(icon), action);
	}

	private void addButton(String text, Image icon, Runnable action) {
		RedButton button = new RedButton(TitleCase.of(text)) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		if (icon != null) button.icon(icon);
		button.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(button);
		pos = button.bottom() + GAP;
	}
}
