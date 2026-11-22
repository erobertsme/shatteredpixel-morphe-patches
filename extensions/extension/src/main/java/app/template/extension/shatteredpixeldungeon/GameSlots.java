package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The games in progress on the main menu. Picking one shows the same preview as Game Saves, with Continue and Erase, its deepest floor and
 * its seed, which can start a new game. Each time the game saves itself during play, the preview of that game is kept beside it. Seeds of
 * unfinished games can also be typed in for a new game.
 */
public final class GameSlots {

	private static final String TAG = "GameSlots";
	private static final String KEY_PREVIEW_OFF = "qol_game_preview_off";
	private static final String KEY_SEEDS_OFF = "qol_reuse_seeds_off";

	private static final ExecutorService writer = Executors.newSingleThreadExecutor(task -> {
		Thread thread = new Thread(task, "game-preview");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		return thread;
	});

	private GameSlots() {
	}

	static boolean previews() {
		return QolSettings.isAvailable() && !CloverlordSettings.get(KEY_PREVIEW_OFF);
	}

	static void previews(boolean on) {
		CloverlordSettings.set(KEY_PREVIEW_OFF, !on);
	}

	static boolean reuseSeeds() {
		return QolSettings.isAvailable() && !CloverlordSettings.get(KEY_SEEDS_OFF);
	}

	static void reuseSeeds(boolean on) {
		CloverlordSettings.set(KEY_SEEDS_OFF, !on);
	}

	/**
	 * Called at the start of StartScene.SaveSlotButton.onClick. Returns true when the game's preview was shown instead of the game's own
	 * window. Empty slots start a new game as usual.
	 */
	public static boolean open(Object button) {
		if (!previews()) return false;
		try {
			if (Reflect.get(button, "newGame") == Boolean.TRUE) return false;
			int slot = (Integer) Reflect.get(button, "slot");
			GamesInProgress.Info info = GamesInProgress.check(slot);
			GameSaves.Save save = info == null ? null : preview(slot, info);
			if (save == null) return false;
			EditorWindow.show(new GameSlotWindow(slot, info, save));
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not show the game", e);
			return false;
		}
	}

	/**
	 * Called with the games the custom seed box checks a typed seed against. With seeds of unfinished games allowed, there are none.
	 */
	public static ArrayList<?> seedCheckGames(ArrayList<?> games) {
		return reuseSeeds() ? new ArrayList<>() : games;
	}

	/**
	 * Called at the end of Dungeon.saveAll. When the game saves itself during play, keeps a preview of it beside it: the hero, the last
	 * messages and a picture of the dungeon, read the way Game Saves are.
	 */
	public static void afterSaveAll() {
		if (!previews() || GameSaves.isCopying() || !(Game.scene() instanceof GameScene)) return;
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) return;
		try {
			File folder = GameSaves.slotFolder(GamesInProgress.curSlot);
			if (folder == null || !folder.isDirectory()) return;
			Properties info = GameSaves.details("", false);
			UndoScene scene = UndoScene.capture(false);
			GameSaves.requestPicture(folder);
			writer.execute(() -> {
				try {
					GameSaves.writeExtras(folder, info, scene);
				} catch (Throwable e) {
					Log.e(TAG, "Could not keep the game's preview", e);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Could not keep the game's preview", e);
		}
	}

	/**
	 * Called at the start of Dungeon.deleteGame. Removes the kept preview with the game, so a new game in the slot doesn't show it.
	 */
	public static void onDeleteGame(int slot) {
		File folder = GameSaves.slotFolder(slot);
		if (folder != null) GameSaves.stripSaveOnlyFiles(folder);
	}

	/**
	 * The preview kept beside the game in [slot], one made from the game's own details when the game hasn't kept one yet, or null when the
	 * game's folder can't be found.
	 */
	private static GameSaves.Save preview(int slot, GamesInProgress.Info info) {
		File folder = GameSaves.slotFolder(slot);
		if (folder == null || !folder.isDirectory()) return null;
		GameSaves.Save save = GameSaves.Save.read(folder);
		if (save != null) return save;
		Properties details = new Properties();
		details.setProperty("label", "Depth " + info.depth + ", " + InventoryEditor.capitalize(info.heroClass.title()) + " Lv " + info.level);
		details.setProperty("depth", String.valueOf(info.depth));
		details.setProperty("hp", String.valueOf(info.hp));
		details.setProperty("ht", String.valueOf(info.ht));
		return GameSaves.Save.of(folder, details);
	}

	/**
	 * The seed code of [info]'s game, or null for a daily game, whose seed is the day's.
	 */
	static String seedCode(GamesInProgress.Info info) {
		if (info.daily || info.seed < 0 || info.seed >= DungeonSeed.TOTAL_SEEDS) return null;
		return DungeonSeed.convertToCode(info.seed);
	}

	/**
	 * Starts a new game as [heroClass] from [seed] in the first empty slot, leaving every other game as it is. Returns false when every slot
	 * is taken.
	 */
	static boolean startFromSeed(long seed, HeroClass heroClass) {
		int slot = GamesInProgress.firstEmpty();
		if (slot < 0) return false;
		GamesInProgress.curSlot = slot;
		GamesInProgress.selectedClass = heroClass;
		Dungeon.hero = null;
		Dungeon.daily = false;
		Dungeon.dailyReplay = false;
		Dungeon.seed = seed;
		Dungeon.customSeedText = DungeonSeed.convertToCode(seed);
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		Game.switchScene(InterlevelScene.class);
		return true;
	}
}
