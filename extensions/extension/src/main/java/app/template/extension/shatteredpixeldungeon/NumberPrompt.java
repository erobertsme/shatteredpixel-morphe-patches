package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextInput;

import java.lang.reflect.Field;

/**
 * A number input with Min and Max buttons that apply the limits straight away.
 */
final class NumberPrompt {

	private static final String TAG = "NumberPrompt";
	private static final int BUTTON_HEIGHT = 16;
	private static final int GAP = 2;

	interface Setter {
		void set(int value);
	}

	private NumberPrompt() {
	}

	static void show(String label, int current, int min, int max, Setter setter, Runnable after) {
		WndTextInput input = new WndTextInput(label, null, String.valueOf(current), 7, false, "Set", "Cancel") {
			@Override
			public void onSelect(boolean positive, String text) {
				if (!positive) return;
				try {
					setter.set(clamp(Integer.parseInt(text.trim().replace("+", "").replace("%", "")), min, max));
				} catch (NumberFormatException ignored) {
					return;
				}
				after.run();
			}
		};
		addLimitButtons(input, min, max, setter, after);
		Game.scene().addToFront(input);
	}

	private static void addLimitButtons(Window window, int min, int max, Setter setter, Runnable after) {
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			int height = heightField.getInt(window);
			float half = (width - GAP) / 2f;

			RedButton minButton = new RedButton("Min (" + min + ")", 7) {
				@Override
				public void onClick() {
					window.hide();
					setter.set(min);
					after.run();
				}
			};
			minButton.setRect(0, height + GAP, half, BUTTON_HEIGHT);
			window.add(minButton);

			RedButton maxButton = new RedButton("Max (" + max + ")", 7) {
				@Override
				public void onClick() {
					window.hide();
					setter.set(max);
					after.run();
				}
			};
			maxButton.setRect(half + GAP, height + GAP, half, BUTTON_HEIGHT);
			window.add(maxButton);

			window.resize(width, height + GAP + BUTTON_HEIGHT);
			relayoutTextBox(window);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the min and max buttons", e);
		}
	}

	/**
	 * The text box is a separate input widget placed on screen when it is laid out, so it has to be laid out again
	 * after the window moves.
	 */
	private static void relayoutTextBox(Window window) throws ReflectiveOperationException {
		Field field = WndTextInput.class.getDeclaredField("textBox");
		field.setAccessible(true);
		TextInput textBox = (TextInput) field.get(window);
		if (textBox == null) return;
		textBox.setRect(textBox.left(), textBox.top(), textBox.width(), textBox.height());
	}

	static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
