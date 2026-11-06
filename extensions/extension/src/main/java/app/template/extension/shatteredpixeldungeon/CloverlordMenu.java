package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.Image;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.PointerArea;
import com.watabou.input.PointerEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class CloverlordMenu {

	private static final String TAG = "CloverlordMenu";
	private static final String LABEL = "Extras";
	private static final int GAP = 2;
	private static final int BTN_HEIGHT = 20;

	public static final String HIDE_SUPPORT = "hide_support_button";

	private static WeakReference<StyledButton> titleButton;

	private CloverlordMenu() {
	}

	/**
	 * Replaced by the patch with the version of the patches.
	 */
	public static String patchesVersion() {
		return "";
	}

	public static void show() {
		Game.scene().addToFront(new CloverlordWindow());
	}

	/**
	 * Called at the end of TitleScene.create.
	 * Portrait: adds a full width row below the existing buttons.
	 * Landscape: makes the bottom row four buttons wide and adds the menu button at its end.
	 */
	public static void onTitleCreated(Group scene) {
		placeTitleButton(scene);
		GameSaves.checkFinishedGames(scene);
	}

	private static void placeTitleButton(Group scene) {
		try {
			StyledButton play = titleButton(scene, "btnPlay");
			StyledButton support = titleButton(scene, "btnSupport");
			StyledButton rankings = titleButton(scene, "btnRankings");
			StyledButton settings = titleButton(scene, "btnSettings");
			StyledButton changes = titleButton(scene, "btnChanges");
			StyledButton about = titleButton(scene, "btnAbout");

			StyledButton button = new StyledButton(Chrome.Type.GREY_BUTTON_TR, LABEL) {
				@Override
				public void onClick() {
					show();
				}
			};
			button.icon(Icons.get(Icons.TALENT));
			scene.add(button);
			titleButton = new WeakReference<>(button);
			keepFaderOnTop(scene);

			boolean landscape = support.top() == play.top();
			if (hidesSupport()) {
				support.visible = false;
				support.active = false;
				button.setRect(support.left(), support.top(), support.width(), support.height());
				return;
			}
			if (!landscape) {
				float gap = rankings.top() - support.bottom();
				button.setRect(play.left(), about.bottom() + gap, play.width(), play.height());
				return;
			}

			float left = settings.left();
			float rowWidth = about.right() - left;
			float width = (float) Math.floor((rowWidth - 3 * GAP) / 4f);
			float top = settings.top();
			float height = settings.height();
			settings.setRect(left, top, width, height);
			changes.setRect(settings.right() + GAP, top, width, height);
			about.setRect(changes.right() + GAP, top, width, height);
			button.setRect(about.right() + GAP, top, left + rowWidth - about.right() - GAP, height);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the title screen button", e);
		}
	}

	/**
	 * Called at the end of TitleScene.updateFade. Fades the menu button with the rest of the title buttons.
	 */
	public static void onTitleFade(Group scene) {
		StyledButton button = titleButton == null ? null : titleButton.get();
		if (button == null) return;
		try {
			StyledButton play = titleButton(scene, "btnPlay");
			float alpha = play.alpha();
			button.enable(alpha != 0);
			button.alpha(alpha);
			if (hidesSupport()) titleButton(scene, "btnSupport").active = false;
		} catch (Exception e) {
			Log.e(TAG, "Could not fade the title screen button", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void keepFaderOnTop(Group scene) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		for (Gizmo member : new ArrayList<>((ArrayList<Gizmo>) membersField.get(scene))) {
			if (member != null && member.getClass().getName().endsWith("PixelScene$Fader")) scene.bringToFront(member);
		}
	}

	private static boolean hidesSupport() {
		return SupporterRewards.isAvailable() && CloverlordSettings.get(HIDE_SUPPORT);
	}

	private static StyledButton titleButton(Group scene, String name) throws ReflectiveOperationException {
		Field field = scene.getClass().getDeclaredField(name);
		field.setAccessible(true);
		StyledButton button = (StyledButton) field.get(scene);
		if (button == null) throw new NoSuchFieldException(name + " is not set");
		return button;
	}

	/**
	 * Called at the end of the WndGame constructor. Adds the Extras, Character and Inventory buttons below the existing buttons.
	 */
	public static void onGameMenuCreated(Window window) {
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			int height = heightField.getInt(window);

			height = addGameMenuButton(window, LABEL, Icons.TALENT, width, height, CloverlordMenu::show);
			boolean inRun = Dungeon.hero != null && Dungeon.hero.isAlive();
			if (inRun && EditorSwitches.character()) {
				height = addGameMenuButton(window, "Character", heroAvatar(), width, height, () -> CharacterWindow.openFrom(CloverlordMenu::showGameMenu, CharacterWindow.TAB_STATS, 1));
			}
			if (inRun && EditorSwitches.inventory()) {
				height = addGameMenuButton(window, "Inventory", Icons.BACKPACK, width, height, InventoryWindow::open);
			}
			if (inRun && EditorSwitches.floor()) {
				height = addGameMenuButton(window, "Floor", Icons.STAIRS_LARGE, width, height, () -> FloorWindow.open(CloverlordMenu::showGameMenu));
			}
			if (inRun && EditorSwitches.teleport()) {
				height = addGameMenuButton(window, "Teleport", Icons.BUFFS, width, height, () -> EditorWindow.show(new DepthPickerWindow()));
			}
			if (inRun && SaveBackupActivity.isAvailable()) {
				height = addGameMenuButton(window, "Game Saves", Icons.SCROLL_COLOR, width, height, () -> EditorWindow.show(new GameSavesWindow(0, true)));
			}
			window.resize(width, height);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the game menu buttons", e);
		}
	}

	/**
	 * Called at the end of the WndHero constructor. Adds a hunger summary to the first tab when the Hunger editor
	 * patch is applied, which opens the hunger settings when tapped, and an Edit button for the selected tab.
	 */
	public static void onHeroWindowCreated(Window window) {
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		boolean character = EditorSwitches.character();
		boolean hunger = EditorSwitches.hunger();
		if (!character && !hunger) return;
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			float y = heightField.getInt(window) + GAP;

			List<Gizmo> summary = new ArrayList<>();
			if (hunger) {
				float top = y;
				y = addHungerSummary(window, width, y, summary);
				PointerArea area = new PointerArea(0, top, width, y - top) {
					@Override
					public void onClick(PointerEvent event) {
						window.hide();
						HungerWindow.open(CloverlordMenu::showHeroWindow);
					}
				};
				window.add(area);
				summary.add(area);
				y += 2 * GAP;
			}

			if (character) {
				RedButton edit = new RedButton(editLabel(WndHero.lastIdx), 7) {
					private int shownTab = -1;

					@Override
					public void update() {
						super.update();
						if (shownTab == WndHero.lastIdx) return;
						shownTab = WndHero.lastIdx;
						icon(Icons.get(tabIcon(shownTab)));
						text(editLabel(shownTab));
					}

					@Override
					public void onClick() {
						window.hide();
						CharacterWindow.openFrom(CloverlordMenu::showHeroWindow, editorTabFor(WndHero.lastIdx), 1);
					}
				};
				edit.icon(Icons.get(tabIcon(WndHero.lastIdx)));
				edit.setRect(0, y, width, 16);
				window.add(edit);
				y += 16;
			} else {
				y -= 2 * GAP;
			}

			Gizmo watcher = new Gizmo() {
				private int shownTab = -1;

				@Override
				public void update() {
					super.update();
					if (shownTab == WndHero.lastIdx) return;
					shownTab = WndHero.lastIdx;
					for (Gizmo gizmo : summary) {
						gizmo.visible = shownTab == 0;
						gizmo.active = shownTab == 0;
					}
				}
			};
			window.add(watcher);
			for (Gizmo gizmo : summary) {
				gizmo.visible = WndHero.lastIdx == 0;
				gizmo.active = WndHero.lastIdx == 0;
			}
			window.resize(width, (int) y);
			window.offset(0, 0);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the hero window buttons", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static float addHungerSummary(Window window, int width, float y, List<Gizmo> added) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		List<Gizmo> members = (List<Gizmo>) membersField.get(window);
		int before = members.size();

		RenderedTextBlock title = PixelScene.renderTextBlock("Hunger", 7);
		title.hardlight(0xFFFF44);
		title.setPos(0, y);
		window.add(title);

		RenderedTextBlock hint = PixelScene.renderTextBlock("Tap to Edit", 6);
		hint.hardlight(0x999999);
		hint.setPos(width - hint.width(), y + (title.height() - hint.height()) / 2f);
		window.add(hint);
		y = title.bottom() + 2;

		boolean disabled = HungerEditor.isDisabled();
		y = HungerWindow.addHungerBar(window, 0, y, width, HungerEditor.notHungryTurns(), HungerEditor.hungryTurns(), disabled) + 2;

		RenderedTextBlock starving = PixelScene.renderTextBlock("Starving: " + HungerEditor.describeStarving(HungerEditor.maxHealth()), 6);
		starving.maxWidth(width);
		starving.setPos(0, y);
		window.add(starving);

		added.addAll(members.subList(before, members.size()));
		return starving.bottom();
	}

	private static String editLabel(int heroTab) {
		if (heroTab == 1) return "Edit Talents";
		if (heroTab == 2) return "Edit Statuses";
		return "Edit Stats";
	}

	private static Icons tabIcon(int heroTab) {
		if (heroTab == 1) return Icons.TALENT;
		if (heroTab == 2) return Icons.BUFFS;
		return Icons.RANKINGS;
	}

	static void showGameMenu() {
		Game.scene().addToFront(new WndGame());
	}

	static void showHeroWindow() {
		Game.scene().addToFront(new WndHero());
	}

	private static int editorTabFor(int heroTab) {
		if (heroTab == 1) return CharacterWindow.TAB_TALENTS;
		if (heroTab == 2) return CharacterWindow.TAB_STATUS;
		return CharacterWindow.TAB_STATS;
	}

	private static Image heroAvatar() {
		try {
			return HeroSprite.avatar(Dungeon.hero);
		} catch (Throwable e) {
			return Icons.get(Icons.STATS);
		}
	}

	private static int addGameMenuButton(Window window, String label, Icons icon, int width, int height, Runnable action) {
		return addGameMenuButton(window, label, Icons.get(icon), width, height, action);
	}

	private static int addGameMenuButton(Window window, String label, Image icon, int width, int height, Runnable action) {
		RedButton button = new RedButton(TitleCase.of(label)) {
			@Override
			public void onClick() {
				window.hide();
				action.run();
			}
		};
		button.icon(icon);
		button.setRect(0, height + GAP, width, BTN_HEIGHT);
		window.add(button);
		return height + GAP + BTN_HEIGHT;
	}
}
