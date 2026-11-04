package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public final class CloverlordMenu {

	private static final String TAG = "CloverlordMenu";
	private static final String LABEL = "Extras";
	private static final int GAP = 2;
	private static final int BTN_HEIGHT = 20;

	private CloverlordMenu() {
	}

	public static void show() {
		Game.scene().addToFront(new CloverlordWindow());
	}

	/**
	 * Called at the end of TitleScene.create. Adds a full width menu button below the existing buttons.
	 */
	public static void onTitleCreated(Group scene) {
		try {
			StyledButton play = titleButton(scene, "btnPlay");
			StyledButton support = titleButton(scene, "btnSupport");
			StyledButton rankings = titleButton(scene, "btnRankings");
			StyledButton about = titleButton(scene, "btnAbout");

			float gap = rankings.top() - Math.max(play.bottom(), support.bottom());
			float left = play.left();
			float width = about.right() - left;

			StyledButton button = new StyledButton(Chrome.Type.GREY_BUTTON_TR, LABEL) {
				@Override
				public void onClick() {
					show();
				}
			};
			button.icon(Icons.get(Icons.TALENT));
			button.setRect(left, about.bottom() + gap, width, play.height());
			scene.add(button);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the title screen button", e);
		}
	}

	private static StyledButton titleButton(Group scene, String name) throws ReflectiveOperationException {
		Field field = scene.getClass().getDeclaredField(name);
		field.setAccessible(true);
		StyledButton button = (StyledButton) field.get(scene);
		if (button == null) throw new NoSuchFieldException(name + " is not set");
		return button;
	}

	/**
	 * Called at the end of the WndGame constructor. Adds the menu button below the existing buttons.
	 */
	public static void onGameMenuCreated(Window window) {
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			int height = heightField.getInt(window);

			RedButton button = new RedButton(LABEL) {
				@Override
				public void onClick() {
					window.hide();
					show();
				}
			};
			button.icon(Icons.get(Icons.TALENT));
			button.setRect(0, height + GAP, width, BTN_HEIGHT);
			window.add(button);
			window.resize(width, height + GAP + BTN_HEIGHT);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the game menu button", e);
		}
	}
}
