package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

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
			if (inRun && CharacterWindow.isAvailable()) {
				height = addGameMenuButton(window, "Character", Icons.STATS, width, height, () -> CharacterWindow.open(CharacterWindow.TAB_STATS, 1));
			}
			if (inRun && InventoryWindow.isAvailable()) {
				height = addGameMenuButton(window, "Inventory", Icons.BACKPACK, width, height, InventoryWindow::open);
			}
			window.resize(width, height);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the game menu buttons", e);
		}
	}

	/**
	 * Called at the end of the WndHero constructor. Adds an Edit button that opens the editor for the selected tab,
	 * and a Hunger button when the Hunger editor patch is applied.
	 */
	public static void onHeroWindowCreated(Window window) {
		if (!CharacterWindow.isAvailable() || Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			int height = heightField.getInt(window);

			boolean hunger = HungerEditor.isAvailable();
			float editWidth = hunger ? (width - GAP) * 0.6f : width;

			RedButton edit = new RedButton("Edit character", 7) {
				@Override
				public void onClick() {
					window.hide();
					CharacterWindow.open(editorTabFor(WndHero.lastIdx), 1);
				}
			};
			edit.icon(Icons.get(Icons.STATS));
			edit.setRect(0, height + GAP, editWidth, 16);
			window.add(edit);

			if (hunger) {
				RedButton hungerButton = new RedButton("Hunger", 7) {
					@Override
					public void onClick() {
						window.hide();
						HungerWindow.open(null);
					}
				};
				hungerButton.setRect(editWidth + GAP, height + GAP, width - editWidth - GAP, 16);
				window.add(hungerButton);
			}

			window.resize(width, height + GAP + 16);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the hero window buttons", e);
		}
	}

	private static int editorTabFor(int heroTab) {
		if (heroTab == 1) return CharacterWindow.TAB_TALENTS;
		if (heroTab == 2) return CharacterWindow.TAB_STATUS;
		return CharacterWindow.TAB_STATS;
	}

	private static int addGameMenuButton(Window window, String label, Icons icon, int width, int height, Runnable action) {
		RedButton button = new RedButton(label) {
			@Override
			public void onClick() {
				window.hide();
				action.run();
			}
		};
		button.icon(Icons.get(icon));
		button.setRect(0, height + GAP, width, BTN_HEIGHT);
		window.add(button);
		return height + GAP + BTN_HEIGHT;
	}
}
