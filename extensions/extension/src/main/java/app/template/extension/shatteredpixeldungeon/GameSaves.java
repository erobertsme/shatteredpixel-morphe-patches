package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;

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
	static final int MAX_SAVES = 10;

	static final class Save {
		final File folder;
		final String label;
		final long time;

		Save(File folder, String label, long time) {
			this.folder = folder;
			this.label = label;
			this.time = time;
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
			File finished = nextFinished();
			if (finished == null) return;
			scene.addToFront(prompt(finished));
		} catch (Throwable e) {
			Log.e(TAG, "Could not check for finished games", e);
		}
	}

	private static ConfirmPrompt prompt(File folder) {
		java.util.regex.Matcher matcher = FOLDER.matcher(folder.getName());
		String slot = matcher.matches() ? matcher.group(1) : "?";
		List<Save> saves = savesIn(folder);
		String latest = saves.isEmpty() ? "" : " Its latest save: " + saves.get(0).title() + ".";
		String message = "Your game in slot " + slot + " has ended. It has " + saves.size() + (saves.size() == 1 ? " save" : " saves") + " left over." + latest
			+ "\n\nDelete them, or keep them? Kept saves can be removed later with Clean Up Saves in Extras on the main menu.";
		return new ConfirmPrompt("Finished Game", message, new String[] { "Delete Saves", "Keep Saves" }, new Runnable[] {
			() -> {
				deleteRecursively(folder);
				askNext();
			},
			() -> {
				CloverlordSettings.set(KEPT_PREFIX + folder.getName(), true);
				askNext();
			},
		});
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
			CloverlordSettings.set(KEPT_PREFIX + folder.getName(), false);
			deleteRecursively(folder);
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
			saves.add(new Save(child, info.getProperty("label", child.getName()), time));
		}
		saves.sort((a, b) -> Long.compare(b.time, a.time));
		return saves;
	}

	/**
	 * Saves the game, then copies it into a new save of this game. Returns an error message, or null when it worked.
	 */
	static String create() {
		try {
			if (list().size() >= MAX_SAVES) return "This game already has " + MAX_SAVES + " saves. Delete one first.";
			Dungeon.saveAll();
			File source = gameFolder();
			File folder = savesFolder();
			if (source == null || folder == null || !source.isDirectory()) return "The game's save folder could not be found.";
			long time = System.currentTimeMillis();
			File target = new File(folder, String.valueOf(time));
			copyFolder(source, target);

			Hero hero = Dungeon.hero;
			Properties info = new Properties();
			info.setProperty("time", String.valueOf(time));
			info.setProperty("label", "Depth " + Dungeon.depth + ", " + InventoryEditor.capitalize(hero.className()) + " Lv " + hero.lvl);
			try (OutputStream out = new FileOutputStream(new File(target, INFO_FILE))) {
				info.store(out, null);
			}
			return null;
		} catch (Throwable e) {
			Log.e(TAG, "Could not save", e);
			return "Saving failed: " + e.getMessage();
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
				for (File file : current) deleteRecursively(file);
			}
			copyFolder(save.folder, target);
			new File(target, INFO_FILE).delete();
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
			deleteRecursively(folder);
			copyFolder(source, folder);
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
				for (File file : current) deleteRecursively(file);
			}
			copyFolder(folder, target);
			deleteRecursively(folder);
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
		deleteRecursively(folder);
	}

	static void delete(Save save) {
		deleteRecursively(save.folder);
	}

	private static void copyFolder(File source, File target) throws IOException {
		if (!target.isDirectory() && !target.mkdirs()) throw new IOException("Could not create " + target);
		File[] children = source.listFiles();
		if (children == null) return;
		Arrays.sort(children);
		for (File child : children) {
			File destination = new File(target, child.getName());
			if (child.isDirectory()) {
				copyFolder(child, destination);
				continue;
			}
			try (InputStream in = new FileInputStream(child); OutputStream out = new FileOutputStream(destination)) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
			}
		}
	}

	private static void deleteRecursively(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) deleteRecursively(child);
		}
		file.delete();
	}

	private static long parseLong(String text, long fallback) {
		try {
			return text == null ? fallback : Long.parseLong(text);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
