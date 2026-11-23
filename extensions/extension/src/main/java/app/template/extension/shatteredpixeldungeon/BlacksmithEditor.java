package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/**
 * An Edit button in the troll blacksmith's window, for how much favor you have with him and whether he lets you down into the mine,
 * even after his quest is done.
 */
public final class BlacksmithEditor {

	private static final String TAG = "BlacksmithEditor";
	private static final String QUEST = "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith$Quest";
	private static final String WINDOW = "com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith";
	private static final String KEY_MINE_OPEN = "blacksmith_mine_open";

	private static Object pendingTroll;

	private BlacksmithEditor() {
	}

	/**
	 * Called at the start of the WndBlacksmith constructor with the blacksmith.
	 */
	public static void beforeWindow(Object troll) {
		pendingTroll = troll;
	}

	/**
	 * Called at the end of the WndBlacksmith constructor. Adds the Edit button.
	 */
	public static void afterWindow(Object window) {
		Object troll = pendingTroll;
		pendingTroll = null;
		if (!(window instanceof Window) || troll == null || !EditorSwitches.floor()) return;
		try {
			WindowHeader.addEdit((Window) window, () -> {
				((Window) window).hide();
				EditorWindow.show(new BlacksmithWindow(() -> reopen(troll)));
			});
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the Edit button", e);
		}
	}

	private static void reopen(Object troll) {
		try {
			Class<?> type = Class.forName(WINDOW);
			Object window = type.getConstructors()[0].newInstance(troll, Dungeon.hero);
			EditorWindow.show((Window) window);
		} catch (Throwable e) {
			Log.e(TAG, "Could not reopen the blacksmith", e);
		}
	}

	static int favor() {
		try {
			return Reflect.field(Class.forName(QUEST), "favor").getInt(null);
		} catch (Throwable e) {
			return 0;
		}
	}

	static void favor(int value) {
		try {
			Reflect.field(Class.forName(QUEST), "favor").setInt(null, Math.max(0, value));
		} catch (Throwable e) {
			Log.e(TAG, "Could not set the favor", e);
		}
	}

	static boolean mineOpen() {
		return GameSettings.override(KEY_MINE_OPEN) == Boolean.TRUE;
	}

	static void mineOpen(boolean open) {
		GameSettings.store(KEY_MINE_OPEN, open);
	}

	/**
	 * Called with each answer the caves floor checks before letting you down into the mine: whether the quest was given, started or
	 * completed. While the blacksmith lets you down, the way is open as during his quest.
	 */
	public static boolean questGiven(boolean given) {
		return given || mineOpen();
	}

	public static boolean questStarted(boolean started) {
		return started || mineOpen();
	}

	public static boolean questCompleted(boolean completed) {
		return completed && !mineOpen();
	}

	/**
	 * Favor and the way down into the mine.
	 */
	static final class BlacksmithWindow extends EditorWindow {

		private static final String INFO = "Blacksmith\n\n"
			+ "_Favor:_ what the blacksmith's services cost you. Long-press - or + to change it by 10 steps, or tap it to type it.\n"
			+ "_Let me into the mine:_ he lets you down into the mine below, even once his quest is done. Leaving the mine doesn't end the quest again.";

		private final Runnable back;

		BlacksmithWindow(Runnable back) {
			super();
			this.back = back;
			addTitle("Blacksmith", INFO);
			int favor = favor();
			addStepper("Favor", String.valueOf(favor), favor, 50, 0, 99999, BlacksmithEditor::favor);
			addCheckBox("Let me into the mine", mineOpen(), () -> mineOpen(!mineOpen()));
			pos += GAP;
			finishWithBack(back);
		}

		@Override
		protected void reopen() {
			show(new BlacksmithWindow(back));
		}
	}
}
