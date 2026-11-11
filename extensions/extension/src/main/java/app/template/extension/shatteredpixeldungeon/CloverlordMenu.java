package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
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
			titleFaded = false;
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

	private static boolean titleFaded;

	/**
	 * Called at the end of TitleScene.updateFade. Fades the menu button with the rest of the title buttons.
	 */
	public static void onTitleFade(Group scene) {
		StyledButton button = titleButton == null ? null : titleButton.get();
		if (button == null || titleFaded) return;
		try {
			StyledButton play = titleButton(scene, "btnPlay");
			float alpha = play.alpha();
			button.enable(alpha != 0);
			button.alpha(alpha);
			if (hidesSupport()) titleButton(scene, "btnSupport").active = false;
			titleFaded = alpha >= 1f;
		} catch (Exception e) {
			Log.e(TAG, "Could not fade the title screen button", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void keepFaderOnTop(Group scene) throws ReflectiveOperationException {
		for (Gizmo member : new ArrayList<>(Reflect.members(scene))) {
			if (member != null && member.getClass().getName().endsWith("PixelScene$Fader")) scene.bringToFront(member);
		}
	}

	private static boolean hidesSupport() {
		return SupporterRewards.isAvailable() && CloverlordSettings.get(HIDE_SUPPORT);
	}

	private static StyledButton titleButton(Group scene, String name) throws ReflectiveOperationException {
		Field field = Reflect.field(scene.getClass(), name);
		StyledButton button = (StyledButton) field.get(scene);
		if (button == null) throw new NoSuchFieldException(name + " is not set");
		return button;
	}

	/**
	 * Called at the end of the WndGame constructor. Adds the Extras, Character and Inventory buttons below the existing buttons.
	 */
	public static void onGameMenuCreated(Window window) {
		try {
			int width = Reflect.width(window);
			int height = Reflect.height(window);

			height = addGameMenuButton(window, LABEL, Icons.TALENT, width, height, CloverlordMenu::show);
			boolean inRun = Dungeon.hero != null && Dungeon.hero.isAlive();
			if (inRun && EditorSwitches.character()) {
				height = addGameMenuButton(window, "Character", heroAvatar(), width, height, () -> CharacterWindow.openFrom(CloverlordMenu::showGameMenu, CharacterWindow.TAB_STATS, 1));
			}
			if (inRun && EditorSwitches.inventory()) {
				height = addGameMenuButton(window, "Inventory", Icons.BACKPACK_LRG, width, height, InventoryWindow::open);
			}
			if (inRun && EditorSwitches.floor()) {
				height = addGameMenuButton(window, "Floor", Icons.STAIRS_LARGE, width, height, () -> FloorWindow.open(CloverlordMenu::showGameMenu));
			}
			if (inRun && EditorSwitches.teleport()) {
				height = addGameMenuButton(window, "Teleport", Teleport.largeIcon(), width, height, () -> DepthPickerWindow.open(CloverlordMenu::showGameMenu));
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
			int width = Reflect.width(window);
			float y = Reflect.height(window) + GAP;

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

			float listBottom = y - 2 * GAP;
			if (character) {
				RedButton edit = new RedButton(editLabel(WndHero.lastIdx), 7) {
					@Override
					public void onClick() {
						window.hide();
						CharacterWindow.openFrom(CloverlordMenu::showHeroWindow, editorTabFor(WndHero.lastIdx), 1);
					}
				};
				edit.icon(Icons.get(tabIcon(WndHero.lastIdx)));
				edit.setRect(0, y, width, 16);
				window.add(edit);
				editButton = edit;
				y += 16;
			} else {
				y -= 2 * GAP;
				editButton = null;
			}

			heroWindow = new WeakReference<>(window);
			heroSummary = summary;
			showHeroTab();
			fillTalentList(window, width, listBottom);
			window.resize(width, (int) y + 1);
			window.offset(0, 0);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the hero window buttons", e);
		}
	}

	private static WeakReference<Window> heroWindow = new WeakReference<>(null);
	private static List<Gizmo> heroSummary = new ArrayList<>();
	private static RedButton editButton;

	/**
	 * Called at the end of WndTabbed.select. When the hero window changes tab, matches the summary and the edit button to it.
	 */
	public static void afterTabSelected(Object window) {
		if (window != null && window == heroWindow.get()) showHeroTab();
	}

	private static void showHeroTab() {
		int tab = WndHero.lastIdx;
		for (Gizmo gizmo : heroSummary) {
			gizmo.visible = tab == 0;
			gizmo.active = tab == 0;
		}
		RedButton edit = editButton;
		if (edit == null) return;
		edit.icon(Icons.get(tabIcon(tab)));
		edit.text(editLabel(tab));
	}

	/**
	 * Lets the talents tab use the height the hunger summary adds to the window, instead of leaving it blank.
	 */
	private static void fillTalentList(Window window, int width, float bottom) {
		try {
			Field field = Reflect.field(window.getClass(), "talents");
			Object talents = field.get(window);
			if (!(talents instanceof com.watabou.noosa.ui.Component)) return;
			com.watabou.noosa.ui.Component tab = (com.watabou.noosa.ui.Component) talents;
			if (bottom <= tab.height()) return;
			tab.setRect(tab.left(), tab.top(), width, bottom - tab.top());
			scrollToBottom(tab);
		} catch (Throwable e) {
			Log.e(TAG, "Could not resize the talents tab", e);
		}
	}

	/**
	 * Scrolls the talents list to the bottom for its new height, as the game does when the window opens.
	 */
	private static void scrollToBottom(com.watabou.noosa.ui.Component tab) throws ReflectiveOperationException {
		Field paneField = Reflect.field(tab.getClass(), "pane");
		Object pane = paneField.get(tab);
		if (!(pane instanceof com.watabou.noosa.ui.Component)) return;
		com.watabou.noosa.ui.Component scroll = (com.watabou.noosa.ui.Component) pane;
		Object content = Reflect.method(pane.getClass(), "content").invoke(pane);
		float contentHeight = content instanceof com.watabou.noosa.ui.Component ? ((com.watabou.noosa.ui.Component) content).height() : 0;
		float target = Math.max(0, contentHeight - scroll.height());
		Reflect.method(pane.getClass(), "scrollTo", float.class, float.class).invoke(pane, 0f, target);
		Reflect.method(tab.getClass(), "layout").invoke(tab);
	}

	@SuppressWarnings("unchecked")
	private static float addHungerSummary(Window window, int width, float y, List<Gizmo> added) throws ReflectiveOperationException {
		List<Gizmo> members = Reflect.members(window);
		int before = members.size();

		float bottom = HungerWindow.addSummary(window, 0, y, width);
		added.addAll(members.subList(before, members.size()));
		return bottom;
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
