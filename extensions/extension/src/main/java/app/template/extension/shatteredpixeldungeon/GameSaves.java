package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Extra saves of the game in progress. Each game gets its own folder in the save folder, named after its save slot
 * and seed (game1_1a2b3c), next to the game's own folder, so saves of one game can never be loaded into another and backups include them.
 */
final class GameSaves {

	private static final String TAG = "GameSaves";
	private static final String OLD_ROOT = "morphe_saves";
	private static final String KEPT_PREFIX = "gamesave_kept_";
	private static final java.util.regex.Pattern FOLDER = java.util.regex.Pattern.compile("game(\\d+)_([0-9a-f]+)");
	private static final String INFO_FILE = "morphe_save_info.properties";
	private static final String PREVIEW_FILE = "morphe_save_preview.bin";
	private static final String GAME_FILE = "game.dat";
	static final int MAX_SAVES = 10;
	static final int MAX_AUTO_SAVES = 5;

	static final class Save {
		final File folder;
		final String label;
		final long time;
		final boolean locked;
		final boolean auto;

		Save(File folder, String label, long time, boolean locked, boolean auto) {
			this.folder = folder;
			this.label = label;
			this.time = time;
			this.auto = auto;
			this.locked = locked;
		}

		/**
		 * What the game looked like when this save was made, or null for saves made before previews were kept.
		 */
		UndoScene preview() {
			File file = new File(folder, PREVIEW_FILE);
			if (!file.isFile()) return null;
			try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
				return UndoScene.read(in);
			} catch (IOException e) {
				return null;
			}
		}

		/**
		 * The hero as they were in this save, or null if it can't be read.
		 */
		Hero hero() {
			File file = new File(folder, GAME_FILE);
			if (!file.isFile()) return null;
			try {
				return SnapshotHero.read(FileHelpers.read(file));
			} catch (IOException e) {
				return null;
			}
		}

		String title() {
			return label + " - " + new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(time));
		}
	}

	private GameSaves() {
	}

	static boolean isAvailable() {
		return SaveBackupActivity.isAvailable() && GameSettings.inGame();
	}

	private static File saveRoot() {
		return SaveLocation.currentRoot();
	}

	private static File gameFolder() {
		File root = saveRoot();
		if (root == null) return null;
		return new File(root, GamesInProgress.gameFolder(GamesInProgress.curSlot));
	}

	private static File savesFolder() {
		File root = saveRoot();
		if (root == null) return null;
		moveOldFolders(root);
		String name = GamesInProgress.gameFolder(GamesInProgress.curSlot) + "_" + Long.toHexString(Dungeon.seed);
		return new File(root, name);
	}

	/**
	 * Earlier versions kept game saves inside a morphe_saves folder. Moves them next to the game folders.
	 */
	private static void moveOldFolders(File root) {
		File old = new File(root, OLD_ROOT);
		File[] children = old.listFiles();
		if (children == null) return;
		for (File child : children) {
			File target = new File(root, child.getName());
			if (!target.exists()) child.renameTo(target);
		}
		String[] left = old.list();
		if (left == null || left.length == 0) old.delete();
	}

	/**
	 * Called when the title screen opens. Asks what to do with the saves of each game that has ended, one game at
	 * a time.
	 */
	static void checkFinishedGames(com.watabou.noosa.Group scene) {
		try {
			if (!SaveBackupActivity.isAvailable()) return;
			if (CrashLog.showIfAny(scene)) return;
			File finished = nextFinished();
			if (finished != null) {
				scene.addToFront(prompt(finished));
				return;
			}
			ConfirmPrompt undoOnly = damagedWithUndoSteps();
			if (undoOnly != null) scene.addToFront(undoOnly);
		} catch (Throwable e) {
			Log.e(TAG, "Could not check for finished games", e);
		}
	}

	private static ConfirmPrompt prompt(File folder) {
		java.util.regex.Matcher matcher = FOLDER.matcher(folder.getName());
		int slot = matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
		List<Save> saves = savesIn(folder);
		boolean damaged = slot > 0 && isDamaged(slot);
		boolean canRestore = slot > 0 && !saves.isEmpty() && slotFree(slot);
		String latest = saves.isEmpty() ? "" : " Its latest save: " + saves.get(0).title() + ".";
		String count = saves.size() + (saves.size() == 1 ? " save" : " saves");
		String message = damaged
			? "Your game in slot " + slot + " can't be read. Its save file may have been damaged, for example if the phone shut down while the game was saving. It has " + count + "." + latest
				+ "\n\nRestore a save to keep playing from it, keep the saves for later, or delete them."
			: "Your game in slot " + (slot > 0 ? String.valueOf(slot) : "?") + " has ended. It has " + count + " left over." + latest
				+ "\n\nDelete them, or keep them? If any are locked, you'll be asked about those next. Kept saves can be removed later with Clean Up Saves in Extras on the main menu.";
		Runnable delete = () -> {
			deleteStoredHistory(folder);
			if (deleteUnlocked(folder)) {
				EditorWindow.show(lockedPrompt(folder));
			} else {
				askNext();
			}
		};
		Runnable keep = () -> {
			CloverlordSettings.set(KEPT_PREFIX + folder.getName(), true);
			askNext();
		};
		String title = damaged ? "Damaged Game" : "Finished Game";
		long seed = matcher.matches() ? new java.math.BigInteger(matcher.group(2), 16).longValue() : 0;
		boolean hasUndo = damaged && !UndoHistory.storedSteps(slot, seed).isEmpty();
		Runnable restoreUndo = () -> EditorWindow.show(new RestoreUndoWindow(slot, seed, 0));
		if (!canRestore && hasUndo) return new ConfirmPrompt(title, message, new String[] { "Restore an Undo Step", "Keep Saves", "Delete Saves" }, new Runnable[] { restoreUndo, keep, delete });
		if (!canRestore) return new ConfirmPrompt(title, message, new String[] { "Delete Saves", "Keep Saves" }, new Runnable[] { delete, keep });
		Runnable restore = () -> EditorWindow.show(new RestoreSaveWindow(folder, slot, 0));
		if (hasUndo) return new ConfirmPrompt(title, message, new String[] { "Restore a Save", "Restore an Undo Step", "Keep Saves", "Delete Saves" }, new Runnable[] { restore, restoreUndo, keep, delete });
		if (damaged) return new ConfirmPrompt(title, message, new String[] { "Restore a Save", "Keep Saves", "Delete Saves" }, new Runnable[] { restore, keep, delete });
		return new ConfirmPrompt(title, message, new String[] { "Delete Saves", "Keep Saves", "Restore a Save" }, new Runnable[] { delete, keep, restore });
	}

	/**
	 * A prompt for a damaged game that has kept undo steps but no Game Saves, or null when there is none.
	 */
	private static ConfirmPrompt damagedWithUndoSteps() {
		for (long[] game : UndoHistory.storedGames()) {
			int slot = (int) game[0];
			long seed = game[1];
			if (!isDamaged(slot) || CloverlordSettings.get(KEPT_PREFIX + "undo_" + slot + "_" + seed)) continue;
			List<UndoHistory.StoredStep> steps = UndoHistory.storedSteps(slot, seed);
			if (steps.isEmpty()) continue;
			String message = "Your game in slot " + slot + " can't be read. Its save file may have been damaged, for example if the phone shut down while the game was saving. It has "
				+ steps.size() + (steps.size() == 1 ? " undo step" : " undo steps") + " kept. The latest: " + steps.get(0).title() + "."
				+ "\n\nRestore an undo step to keep playing from it.";
			return new ConfirmPrompt("Damaged Game", message, new String[] { "Restore an Undo Step", "Not Now" }, new Runnable[] {
				() -> EditorWindow.show(new RestoreUndoWindow(slot, seed, 0)),
				() -> CloverlordSettings.set(KEPT_PREFIX + "undo_" + slot + "_" + seed, true),
			});
		}
		return null;
	}

	/**
	 * Whether the game in [slot] has files, but its main save file can't be read.
	 */
	static boolean isDamaged(int slot) {
		File root = saveRoot();
		if (root == null) return false;
		File[] files = new File(root, GamesInProgress.gameFolder(slot)).listFiles();
		return files != null && files.length > 0 && GamesInProgress.check(slot) == null;
	}

	private static boolean slotFree(int slot) {
		try {
			return GamesInProgress.check(slot) == null;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * The saves of a finished game's [folder], newest first.
	 */
	static List<Save> savesOf(File folder) {
		return savesIn(folder);
	}

	/**
	 * Puts [save] back into [slot] as the game in progress there. Anything already in the slot is moved to the app's cache first, so nothing
	 * is lost. Returns an error message, or null when it worked.
	 */
	static String restoreToSlot(Save save, File folder, int slot) {
		try {
			File root = saveRoot();
			if (root == null || !save.folder.isDirectory()) return "That save could not be found.";
			if (!slotFree(slot)) return "Slot " + slot + " already has a game in it.";
			File target = new File(root, GamesInProgress.gameFolder(slot));
			File[] current = target.listFiles();
			if (current != null && current.length > 0) {
				File cache = SaveLocation.cacheDir();
				if (cache != null) FileHelpers.copy(target, new File(cache, "replaced_" + target.getName() + "_" + System.currentTimeMillis()));
				for (File file : current) FileHelpers.delete(file);
			}
			FileHelpers.copy(save.folder, target);
			new File(target, INFO_FILE).delete();
			new File(target, PREVIEW_FILE).delete();
			GamesInProgress.setUnknown(slot);
			CloverlordSettings.set(KEPT_PREFIX + folder.getName(), false);
			Game.resetScene();
			return null;
		} catch (Throwable e) {
			Log.e(TAG, "Could not restore the save", e);
			return "Restoring failed: " + e.getMessage();
		}
	}

	/**
	 * Deletes the kept undo history of the game a finished [folder] belongs to.
	 */
	private static void deleteStoredHistory(File folder) {
		java.util.regex.Matcher matcher = FOLDER.matcher(folder.getName());
		if (!matcher.matches()) return;
		try {
			UndoHistory.deleteStored(Integer.parseInt(matcher.group(1)), new java.math.BigInteger(matcher.group(2), 16).longValue());
		} catch (Throwable e) {
			Log.e(TAG, "Could not delete the undo history", e);
		}
	}

	/**
	 * Asks whether the locked saves left in a finished game's [folder] should go too.
	 */
	private static ConfirmPrompt lockedPrompt(File folder) {
		int locked = savesIn(folder).size();
		String message = (locked == 1 ? "1 locked save is" : locked + " locked saves are") + " left from this game. Delete "
			+ (locked == 1 ? "it" : "them") + " too, or keep " + (locked == 1 ? "it" : "them") + "?";
		return new ConfirmPrompt("Locked Saves", message, new String[] { "Delete Locked Saves", "Keep Locked Saves" }, new Runnable[] {
			() -> {
				FileHelpers.delete(folder);
				CloverlordSettings.set(KEPT_PREFIX + folder.getName(), false);
				askNext();
			},
			() -> {
				CloverlordSettings.set(KEPT_PREFIX + folder.getName(), true);
				askNext();
			},
		});
	}

	/**
	 * Shows the finished game prompt for [folder] again.
	 */
	static void askAgain(File folder) {
		EditorWindow.show(prompt(folder));
	}

	private static void askNext() {
		File next = nextFinished();
		if (next != null) EditorWindow.show(prompt(next));
	}

	private static File nextFinished() {
		for (File folder : finishedFolders()) {
			if (!CloverlordSettings.get(KEPT_PREFIX + folder.getName())) return folder;
		}
		return null;
	}

	/**
	 * Game save folders whose game has ended: the slot is empty, or now holds a different game.
	 */
	static List<File> finishedFolders() {
		List<File> finished = new ArrayList<>();
		File root = saveRoot();
		if (root == null) return finished;
		moveOldFolders(root);
		File[] children = root.listFiles();
		if (children == null) return finished;
		Arrays.sort(children);
		for (File child : children) {
			if (!child.isDirectory()) continue;
			java.util.regex.Matcher matcher = FOLDER.matcher(child.getName());
			if (!matcher.matches()) continue;
			if (isRunning(Integer.parseInt(matcher.group(1)), matcher.group(2))) continue;
			finished.add(child);
		}
		return finished;
	}

	private static boolean isRunning(int slot, String seed) {
		try {
			GamesInProgress.Info info = GamesInProgress.check(slot);
			return info != null && Long.toHexString(info.seed).equals(seed);
		} catch (Throwable e) {
			return true;
		}
	}

	/**
	 * Deletes the saves of every finished game.
	 */
	static int cleanUp() {
		int count = 0;
		for (File folder : finishedFolders()) {
			boolean kept = deleteUnlocked(folder);
			CloverlordSettings.set(KEPT_PREFIX + folder.getName(), kept);
			count++;
		}
		return count;
	}

	/**
	 * This game's saves, newest first.
	 */
	static List<Save> list() {
		return savesIn(savesFolder());
	}

	private static List<Save> savesIn(File folder) {
		List<Save> saves = new ArrayList<>();
		File[] children = folder == null ? null : folder.listFiles();
		if (children == null) return saves;
		for (File child : children) {
			if (!child.isDirectory()) continue;
			Properties info = new Properties();
			File infoFile = new File(child, INFO_FILE);
			if (infoFile.isFile()) {
				try (InputStream in = new FileInputStream(infoFile)) {
					info.load(in);
				} catch (IOException ignored) {
				}
			}
			long time = parseLong(info.getProperty("time"), child.lastModified());
			saves.add(new Save(child, info.getProperty("label", child.getName()), time, Boolean.parseBoolean(info.getProperty("locked")), Boolean.parseBoolean(info.getProperty("auto"))));
		}
		saves.sort((a, b) -> Long.compare(b.time, a.time));
		return saves;
	}

	/**
	 * How many saves count towards the limit. Locked saves don't.
	 */
	static int unlockedCount() {
		return unlockedCount(false);
	}

	/**
	 * How many unlocked saves of one kind there are: auto saves, or saves you made.
	 */
	private static int unlockedCount(boolean auto) {
		int count = 0;
		for (Save save : list()) {
			if (!save.locked && save.auto == auto) count++;
		}
		return count;
	}

	/**
	 * The oldest save that isn't locked, which a new save replaces once the limit is reached, or null if there is none.
	 */
	static Save oldestUnlocked() {
		return oldestUnlocked(false);
	}

	private static Save oldestUnlocked(boolean auto) {
		Save oldest = null;
		for (Save save : list()) {
			if (!save.locked && save.auto == auto && (oldest == null || save.time < oldest.time)) oldest = save;
		}
		return oldest;
	}

	/**
	 * Locks or unlocks [save]. Returns an error message, or null when it worked.
	 */
	static String setLocked(Save save, boolean locked) {
		File infoFile = new File(save.folder, INFO_FILE);
		Properties info = new Properties();
		try {
			if (infoFile.isFile()) {
				try (InputStream in = new FileInputStream(infoFile)) {
					info.load(in);
				}
			} else {
				info.setProperty("time", String.valueOf(save.time));
				info.setProperty("label", save.label);
			}
			info.setProperty("locked", String.valueOf(locked));
			try (OutputStream out = new FileOutputStream(infoFile)) {
				info.store(out, null);
			}
			return null;
		} catch (IOException e) {
			Log.e(TAG, "Could not lock the save", e);
			return "Could not change the lock: " + e.getMessage();
		}
	}

	/**
	 * Saves the game, then copies it into a new save of this game. Once the limit is reached, the oldest save that isn't locked is removed
	 * first. Returns an error message, or null when it worked.
	 */
	static String create() {
		return create(false);
	}

	private static String create(boolean auto) {
		try {
			int limit = auto ? MAX_AUTO_SAVES : MAX_SAVES;
			while (unlockedCount(auto) >= limit) {
				Save oldest = oldestUnlocked(auto);
				if (oldest == null) break;
				delete(oldest);
			}
			Dungeon.saveAll();
			File source = gameFolder();
			File folder = savesFolder();
			if (source == null || folder == null || !source.isDirectory()) return "The game's save folder could not be found.";
			long time = System.currentTimeMillis();
			File target = new File(folder, String.valueOf(time));
			FileHelpers.copy(source, target);

			Hero hero = Dungeon.hero;
			Properties info = new Properties();
			info.setProperty("time", String.valueOf(time));
			info.setProperty("label", (auto ? "Auto: " : "") + "Depth " + Dungeon.depth + place() + ", " + InventoryEditor.capitalize(hero.className()) + " Lv " + hero.lvl);
			if (auto) info.setProperty("auto", "true");
			try (OutputStream out = new FileOutputStream(new File(target, INFO_FILE))) {
				info.store(out, null);
			}
			writePreview(new File(target, PREVIEW_FILE));
			return null;
		} catch (Throwable e) {
			Log.e(TAG, "Could not save", e);
			return "Saving failed: " + e.getMessage();
		}
	}

	/**
	 * The side area the hero is in, such as the Blacksmith's mine, or nothing on the main floors.
	 */
	private static String place() {
		if (Dungeon.branch == 0) return "";
		if (Dungeon.branch == 1 && Dungeon.depth >= 11 && Dungeon.depth <= 14) return " (mine)";
		if (Dungeon.branch == 1 && Dungeon.depth >= 16 && Dungeon.depth <= 19) return " (vault)";
		return " (side area)";
	}

	private static final String KEY_AUTO = "save_auto_on";
	private static volatile boolean autoPending;

	static boolean autoSaves() {
		return CloverlordSettings.get(KEY_AUTO);
	}

	static void autoSaves(boolean on) {
		CloverlordSettings.set(KEY_AUTO, on);
	}

	/**
	 * Called at the start of Dungeon.switchLevel. Notes that the hero is arriving on a floor, or entering or leaving a side area like the
	 * Blacksmith's mine, so the game is saved once they are ready to move there. Loading a game doesn't count.
	 */
	public static void onLevelSwitch() {
		if (!isAvailable() || !autoSaves()) return;
		InterlevelScene.Mode mode = InterlevelScene.mode;
		if (mode == InterlevelScene.Mode.DESCEND || mode == InterlevelScene.Mode.ASCEND || mode == InterlevelScene.Mode.FALL
			|| mode == InterlevelScene.Mode.RETURN) {
			autoPending = true;
		}
	}

	/**
	 * Called at the end of Hero.ready. Makes the auto save noted by [onLevelSwitch].
	 */
	public static void onHeroReady() {
		if (!autoPending) return;
		autoPending = false;
		Game.runOnRenderThread(() -> {
			Hero hero = Dungeon.hero;
			if (hero == null || !hero.isAlive()) return;
			String error = create(true);
			if (error != null) Log.e(TAG, "Auto save failed: " + error);
		});
	}

	private static void writePreview(File file) {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			UndoScene.capture(false).write(out);
		} catch (Throwable e) {
			Log.e(TAG, "Could not keep a preview of the save", e);
			file.delete();
		}
	}

	/**
	 * Replaces the game in progress with [save] and continues from it. Returns an error message, or null when the
	 * game is reloading.
	 */
	static String load(Save save) {
		try {
			File target = gameFolder();
			if (target == null || !save.folder.isDirectory()) return "That save could not be found.";
			File[] current = target.listFiles();
			if (current != null) {
				for (File file : current) FileHelpers.delete(file);
			}
			FileHelpers.copy(save.folder, target);
			new File(target, INFO_FILE).delete();
			new File(target, PREVIEW_FILE).delete();
			GamesInProgress.setUnknown(GamesInProgress.curSlot);
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
			return null;
		} catch (Throwable e) {
			Log.e(TAG, "Could not load", e);
			return "Loading failed: " + e.getMessage();
		}
	}

	/**
	 * Saves the game and keeps a copy of it in [folder], to put back later with [restoreSnapshot].
	 */
	static boolean snapshot(File folder) {
		try {
			Dungeon.saveAll();
			File source = gameFolder();
			if (source == null || !source.isDirectory()) return false;
			FileHelpers.delete(folder);
			FileHelpers.copy(source, folder);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not take a snapshot", e);
			return false;
		}
	}

	/**
	 * Replaces the game in progress with the copy in [folder] and continues from it.
	 */
	static boolean restoreSnapshot(File folder) {
		try {
			File target = gameFolder();
			if (target == null || !folder.isDirectory()) return false;
			File[] current = target.listFiles();
			if (current != null) {
				for (File file : current) FileHelpers.delete(file);
			}
			FileHelpers.copy(folder, target);
			FileHelpers.delete(folder);
			GamesInProgress.setUnknown(GamesInProgress.curSlot);
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not restore the snapshot", e);
			return false;
		}
	}

	static void discardSnapshot(File folder) {
		FileHelpers.delete(folder);
	}

	static void delete(Save save) {
		if (save.locked) return;
		FileHelpers.delete(save.folder);
	}

	/**
	 * Deletes the saves in a finished game's [folder] that aren't locked. Returns true when locked saves were left behind.
	 */
	private static boolean deleteUnlocked(File folder) {
		boolean kept = false;
		for (Save save : savesIn(folder)) {
			if (save.locked) {
				kept = true;
			} else {
				FileHelpers.delete(save.folder);
			}
		}
		if (!kept) FileHelpers.delete(folder);
		return kept;
	}

	private static long parseLong(String text, long fallback) {
		try {
			return text == null ? fallback : Long.parseLong(text);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
