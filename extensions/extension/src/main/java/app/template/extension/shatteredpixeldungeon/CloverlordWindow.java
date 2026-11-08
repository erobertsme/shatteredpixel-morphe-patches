package app.template.extension.shatteredpixeldungeon;

import java.lang.reflect.Field;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.Visual;
import com.watabou.noosa.Group;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.audio.Sample;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.noosa.ColorBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
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
	private static final int SHADE_HEIGHT = 8;
	private static final String CLICK_SOUND = "sounds/click.mp3";
	private static final float MAX_HEIGHT_SHARE = 0.55f;
	private static final int GAP = 2;
	private static final int TITLE_COLOR = 0xFFFF44;
	private static final int UPGRADE_ICON = 50;
	private static final String INFO = "Extras holds the options added by Cloverlord Patches. Tabs and sections only appear when their patch is included.\n\nGeneral: hunger settings and save backups. QoL: quality of life options. Unlocks: unlocks and supporter options. Editors: switch each editor on or off.\n\nThe Editors tab switches each editor on or off. A switched-off editor hides its buttons. Switching off the Hunger editor also puts hunger back to normal until you switch it on again.\n\nUnlocks: shows heroes, guide pages, lore, catalog items and bestiary entries as unlocked without changing your save. Untick to get your real progress back.\n\nSaves: on the main menu, export every save, including game saves, to one zip file or import one. Importing replaces your current saves, keeps a copy of them in the app's cache, and closes the game. In a game, Game Saves keeps up to 10 saves of that game to load back later. When a game ends, the title screen asks whether to delete or keep its saves, and Clean Up Saves deletes any you kept.\n\nSettings you change during a game are kept with that game. The main menu settings are the defaults for new games.\n\n{FOOD}Hunger: change how hunger and starvation work.\n\nList everything on a tile (QoL tab): examining a tile with more than one thing on it, such as a creature standing on an item or a pile of items, lists each one. Pick an item from a pile to pick up that one instead of the top item. Items that ended up inside a grave or chest, like gold dropped by a creature defeated on a grave, are listed too.\n\nUnlimited inventory space and the Weapons, Armor and Rings backpack tabs are in the Inventory editor, under Inventory in the in-game menu.\n\nThe Floor editor is in the in-game menu, with an Edit Floor mode for tapping tiles to edit them, and an Edit button when you examine a tile, trap or item on the floor. Examining an item on the floor also lets you move it, or the whole pile, to another tile.\n\nConfirm actions: choose which taps ask first, such as attacking, walking to a distant tile, using stairs or stepping into danger, so a stray tap can't ruin your run.\n\nQuantum Chest (QoL tab): after you beat a boss, a chest appears beside the exit of that floor. Every quantum chest shares the same storage of 25, 50 or 75 items, so you can drop something off on one boss floor and pick it up on another. Tap the chest while next to it to open it.\n\nBackpack and Toolbar (QoL tab): Backpack sets the size of bags and the Weapons, Armor and Rings tabs. Toolbar has the extra toolbar item slots and the Attack Tile button. Tap i in each for details.\n\nExperience and Loot (QoL tab): change the level limit past which creatures stop giving experience or dropping items: remove it, raise it, give a reduced amount past it, or multiply what you get. Tap i in each for details.\n\nUndo / Redo (General tab, under Saves): a tag that opens undo mode to step back and forward through your recent actions, with settings for how much history is kept, the tag's position and look, and how long each copy takes. Off until you switch it on.\n\nItem Tags (QoL tab): add tags to the screen edge for items you pick, and tap a tag to use its item, like a quickslot. Each tag has its own position, size, colour, icon and opacity.\n\nEnhanced Bestiary (QoL tab): examining a creature, or opening one in the bestiary, adds a Stats button with all of its numbers and an i button in the top right that lists its special abilities, traits, immunities and resistances.\n\nHero stats button (QoL tab): a stats button beside the i button on the hero info window shows your hero's health, experience, strength, accuracy, evasion, damage, armor, speeds and shield.\n\nEnergize more items (QoL tab): lets you energize items in alchemy that the game normally won't. For now that's the Arcane Stylus, worth 5 energy, the same as a Stone of Enchantment.\n\nReroll Catalyst (QoL tab): after transforming a magical catalyst at the alchemy table, a Reroll 5 button under the trinket choices spends 5 energy to show a new set of trinkets.\n\nTap unexplored areas (QoL tab): tapping a part of the map you haven't seen yet walks you to the explored tile you can reach that's closest to where you tapped, instead of doing nothing.\n\nShow interrupted path (QoL tab): when a walk is interrupted and the game shows its resume arrow, the rest of the route is drawn on the map.\n\nShopkeeper (QoL tab): keep enemies out of the shop, stop the shopkeeper running off, and choose how many sold items can be bought back.\n\nSupporter options: keep your supporter rewards and hide the Support button.";

	private float pos;

	private static final int TAB_GENERAL = 0;
	private static final int TAB_QOL = 1;
	private static final int TAB_UNLOCKS = 2;
	private static final int TAB_EDITORS = 3;
	private static final String[] TAB_NAMES = { "General", "QoL", "Unlocks", "Editors" };

	private static int page;
	private ScrollPane scrollPane;

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

		float pageTop = pos;
		int firstMember = memberCount();
		if (page == TAB_EDITORS) buildEditors();
		if (page == TAB_QOL) buildQualityOfLife();
		if (page == TAB_UNLOCKS) buildUnlocks();
		if (page == TAB_GENERAL) buildGeneral();
		scrollIfTall(pageTop, firstMember);
		pos += 2 * GAP;
		addButton("Back", (Image) null, this::back);
		resize(WIDTH, (int) (pos - GAP));
		if (scrollPane != null) {
			scrollPane.setRect(scrollPane.left(), scrollPane.top(), scrollPane.width(), scrollPane.height());
			addScrollHints(scrollPane);
		}
	}

	/**
	 * Arrows and shading at the top and bottom edges of a scrolling page, shown while there is more to scroll to
	 * in that direction.
	 */
	private void addScrollHints(ScrollPane pane) {
		Component content = pane.content();
		ColorBlock topShade = new ColorBlock(pane.width(), SHADE_HEIGHT, 0xFF000000);
		topShade.x = pane.left();
		topShade.y = pane.top();
		ColorBlock bottomShade = new ColorBlock(pane.width(), SHADE_HEIGHT, 0xFF000000);
		bottomShade.x = pane.left();
		bottomShade.y = pane.bottom() - SHADE_HEIGHT;
		Image up = chevron(false, pane.left() + pane.width() / 2f, pane.top() + 1);
		Image down = chevron(true, pane.left() + pane.width() / 2f, pane.bottom() - 1);
		Gizmo watcher = new Gizmo() {
			@Override
			public void update() {
				super.update();
				float scroll = content.camera == null || content.camera.scroll == null ? 0 : content.camera.scroll.y;
				boolean more = scroll < content.height() - pane.height() - 1;
				boolean less = scroll > 1;
				down.visible = more;
				bottomShade.visible = more;
				up.visible = less;
				topShade.visible = less;
			}
		};
		topShade.alpha(0.35f);
		bottomShade.alpha(0.35f);
		add(topShade);
		add(bottomShade);
		add(up);
		add(down);
		add(watcher);
		up.visible = topShade.visible = false;
	}

	private static Image chevron(boolean down, float centerX, float edgeY) {
		Image image = Icons.get(Icons.CHEVRON);
		image.origin.set(image.width() / 2f, image.height() / 2f);
		if (down) image.angle = 180;
		image.x = centerX - image.width() / 2f;
		image.y = down ? edgeY - image.height() : edgeY;
		return image;
	}

	@SuppressWarnings("unchecked")
	private List<Gizmo> members() {
		try {
			Field field = Group.class.getDeclaredField("members");
			field.setAccessible(true);
			return (List<Gizmo>) field.get(this);
		} catch (Throwable e) {
			return null;
		}
	}

	private int memberCount() {
		List<Gizmo> members = members();
		return members == null ? 0 : members.size();
	}

	/**
	 * Puts the page in a scrolling area when the window would be taller than most of the screen.
	 */
	private void scrollIfTall(float pageTop, int firstMember) {
		float maxHeight = PixelScene.uiCamera.height * MAX_HEIGHT_SHARE;
		float pageHeight = pos - pageTop;
		float viewHeight = maxHeight - pageTop - BTN_HEIGHT - 2 * GAP;
		List<Gizmo> members = members();
		if (members == null || pageHeight <= viewHeight || viewHeight < 3 * BTN_HEIGHT) return;
		try {
			List<Gizmo> page = new ArrayList<>(members.subList(firstMember, members.size()));
			ScrollPane pane = new ScrollPane(new Component()) {
				@Override
				public void onClick(float x, float y) {
					tapInside(content(), x, y);
				}
			};
			Component content = pane.content();
			for (Gizmo gizmo : page) {
				remove(gizmo);
				if (gizmo instanceof Component) {
					Component component = (Component) gizmo;
					component.setPos(component.left(), component.top() - pageTop);
				} else if (gizmo instanceof Visual) {
					((Visual) gizmo).y -= pageTop;
				}
				content.add(gizmo);
			}
			content.setSize(WIDTH, pageHeight);
			add(pane);
			pane.setRect(0, pageTop, WIDTH, viewHeight);
			scrollPane = pane;
			pos = pageTop + viewHeight;
		} catch (Throwable e) {
			android.util.Log.e("CloverlordWindow", "Could not make the page scroll", e);
		}
	}

	/**
	 * The scroll pane takes every tap itself, so a tap is passed on to the button under it.
	 */
	@SuppressWarnings("unchecked")
	private static void tapInside(Component content, float x, float y) {
		try {
			Field field = Group.class.getDeclaredField("members");
			field.setAccessible(true);
			for (Gizmo gizmo : new ArrayList<>((List<Gizmo>) field.get(content))) {
				if (!(gizmo instanceof Button) || !gizmo.visible || !gizmo.active) continue;
				Button button = (Button) gizmo;
				if (x < button.left() || x >= button.right() || y < button.top() || y >= button.bottom()) continue;
				Sample.INSTANCE.play(CLICK_SOUND);
				button.onClick();
				return;
			}
		} catch (Throwable e) {
			android.util.Log.e("CloverlordWindow", "Could not pass on the tap", e);
		}
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
			addButton("Undo / Redo", StatIcons.arrow(true), () -> {
				hide();
				EditorWindow.show(new UndoSettingsWindow());
			});
		} else if (SaveBackupActivity.isAvailable()) {
			addLabel("Saves");
			addButton("Export saves", exportIcon(), () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_EXPORT));
			addButton("Import saves", Icons.CHANGES, () -> SaveBackupActivity.launch(SaveBackupActivity.ACTION_IMPORT));
			addButton("Undo / Redo", StatIcons.arrow(true), () -> {
				hide();
				EditorWindow.show(new UndoSettingsWindow());
			});
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
		addButton("Backpack", (Image) null, () -> {
			hide();
			EditorWindow.show(new BackpackSettingsWindow());
		});
		addButton("Toolbar", (Image) null, () -> {
			hide();
			EditorWindow.show(new ToolbarSettingsWindow());
		});
		addButton("Experience", (Image) null, () -> {
			hide();
			EditorWindow.show(new RewardRulesWindow(KillRewards.Kind.EXPERIENCE));
		});
		addButton("Loot", (Image) null, () -> {
			hide();
			EditorWindow.show(new RewardRulesWindow(KillRewards.Kind.LOOT));
		});
		addButton("Item Tags", (Image) null, () -> {
			hide();
			EditorWindow.show(new CustomTagsWindow());
		});
		addLabel("Movement");
		addOption("Show interrupted path", QolSettings.resumePath(), QolSettings::resumePath);
		addOption("Tap unexplored areas", UnexploredTaps.isOn(), UnexploredTaps::setOn);
		addLabel("Information");
		addOption("Enhanced Bestiary", QolSettings.enhancedBestiary(), QolSettings::enhancedBestiary);
		addOption("List everything on a tile", TileContents.isOn(), TileContents::setOn);
		addLabel("Alchemy");
		addOption("Energize more items", EnergizeMore.isOn(), EnergizeMore::setOn);
		addOption("Reroll Catalyst", RerollCatalyst.isOn(), RerollCatalyst::setOn);
		addOption("Hero stats button", QolSettings.heroStatsButton(), QolSettings::heroStatsButton);
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
	}

	private void addSwitch(String text, String key) {
		CheckBox box = new ToggleBox(text) {
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
		CheckBox box = new ToggleBox(text) {
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
		CheckBox box = new ToggleBox(text) {
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
