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
 * Extra saves of the game in progress. Each game gets its own folder, named after its save slot and seed, inside
 * the game's save folder, so saves of one game can never be loaded into another and backups include them.
 */
final class GameSaves {

	private static final String TAG = "GameSaves";
	private static final String ROOT = "morphe_saves";
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
		String name = GamesInProgress.gameFolder(GamesInProgress.curSlot) + "_" + Long.toHexString(Dungeon.seed);
		return new File(new File(root, ROOT), name);
	}

	/**
	 * This game's saves, newest first.
	 */
	static List<Save> list() {
		List<Save> saves = new ArrayList<>();
		File folder = savesFolder();
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
