package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Undo and Redo. Each time your hero is ready for a new action the game is saved and copies of the game file and
 * the current floor's file are kept in memory. Undo and Redo write a copy back and reload the game from it.
 */
@SuppressWarnings("unused")
public final class UndoHistory {

	private static final String TAG = "UndoHistory";

	static final int MODE_LAST = 0;
	static final int MODE_FLOOR = 1;
	static final int MODE_UNLIMITED = 2;
	static final String[] MODES = { "Last actions", "This floor", "Unlimited" };
	static final int DEFAULT_SIZE = 20;
	static final int MAX_SIZE = 200;
	private static final int LIST_LINE_LENGTH = 24;

	private static final String KEY_ON = "qol_undo_on";
	private static final String KEY_MODE = "qol_undo_mode";
	private static final String KEY_SIZE = "qol_undo_size";
	private static final String KEY_FLOOR_ONLY = "qol_undo_floor_only";
	private static final String KEY_NO_BOSS = "qol_undo_no_boss";

	private static final class Snapshot {
		final byte[] game;
		final String levelFile;
		final byte[] level;
		final int depth;
		final int branch;
		final int turn;
		final int hp;
		final int ht;
		UndoScene scene;

		Snapshot(byte[] game, String levelFile, byte[] level, int depth, int branch, int turn, int hp, int ht) {
			this.game = game;
			this.levelFile = levelFile;
			this.level = level;
			this.depth = depth;
			this.branch = branch;
			this.turn = turn;
			this.hp = hp;
			this.ht = ht;
		}

		int size() {
			return game.length + level.length;
		}
	}

	private static final List<Snapshot> history = new ArrayList<>();
	private static int index = -1;
	private static int selected = -1;
	private static String gameId;
	private static boolean skipNext;
	private static boolean pending;

	private static long lastSaveNanos;
	private static long lastCopyNanos;
	private static long slowestNanos;
	private static long totalNanos;
	private static int timedSnapshots;

	private UndoHistory() {
	}

	static boolean isAvailable() {
		return SaveBackupActivity.isAvailable();
	}

	static boolean isOn() {
		return isAvailable() && CloverlordSettings.get(KEY_ON);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_ON, on);
		if (!on) clear();
	}

	static int mode() {
		int mode = CloverlordSettings.getInt(KEY_MODE);
		return mode < MODE_LAST || mode > MODE_UNLIMITED ? MODE_LAST : mode;
	}

	static void mode(int mode) {
		CloverlordSettings.setInt(KEY_MODE, mode);
		trim();
	}

	static int size() {
		int size = CloverlordSettings.getInt(KEY_SIZE);
		return size <= 0 ? DEFAULT_SIZE : Math.min(MAX_SIZE, size);
	}

	static void size(int size) {
		CloverlordSettings.setInt(KEY_SIZE, Math.max(1, Math.min(MAX_SIZE, size)));
		trim();
	}

	static boolean acrossFloors() {
		return !CloverlordSettings.get(KEY_FLOOR_ONLY);
	}

	static void acrossFloors(boolean on) {
		CloverlordSettings.set(KEY_FLOOR_ONLY, !on);
	}

	static boolean duringBosses() {
		return !CloverlordSettings.get(KEY_NO_BOSS);
	}

	static void duringBosses(boolean on) {
		CloverlordSettings.set(KEY_NO_BOSS, !on);
	}

	/**
	 * Called at the end of Hero.ready, when the hero waits for your next action. The snapshot is taken on the
	 * render thread, while the game waits for input.
	 */
	public static void onHeroReady() {
		if (!isOn() || pending) return;
		pending = true;
		Game.runOnRenderThread(() -> {
			pending = false;
			capture();
		});
	}

	private static void capture() {
		Hero hero = Dungeon.hero;
		if (!isOn() || hero == null || !hero.isAlive() || !hero.ready) return;
		String id = GamesInProgress.curSlot + ":" + Dungeon.seed;
		if (!id.equals(gameId)) {
			clear();
			gameId = id;
		}
		if (skipNext) {
			skipNext = false;
			UndoScene.discardLines();
			return;
		}
		try {
			long start = System.nanoTime();
			Dungeon.saveAll();
			long saved = System.nanoTime();
			File root = SaveLocation.currentRoot();
			if (root == null) return;
			String levelFile = GamesInProgress.depthFile(GamesInProgress.curSlot, Dungeon.depth, Dungeon.branch);
			byte[] game = read(new File(root, GamesInProgress.gameFile(GamesInProgress.curSlot)));
			byte[] level = read(new File(root, levelFile));
			long copied = System.nanoTime();
			Snapshot snapshot = new Snapshot(game, levelFile, level, Dungeon.depth, Dungeon.branch, (int) (Statistics.duration + Actor.now()), hero.HP, hero.HT);
			snapshot.scene = UndoScene.capture();
			push(snapshot);
			recordTiming(saved - start, copied - saved);
		} catch (Throwable e) {
			Log.e(TAG, "Could not take a snapshot", e);
		}
	}

	private static void push(Snapshot snapshot) {
		while (history.size() > index + 1) history.remove(history.size() - 1);
		Snapshot last = index >= 0 ? history.get(index) : null;
		boolean newFloor = last != null && (last.depth != snapshot.depth || last.branch != snapshot.branch);
		if (newFloor && (mode() == MODE_FLOOR || !acrossFloors())) history.clear();
		history.add(snapshot);
		index = history.size() - 1;
		trim();
		selected = index;
	}

	private static void trim() {
		if (mode() != MODE_LAST) return;
		while (history.size() > size()) {
			history.remove(0);
			index--;
		}
		if (index < -1) index = -1;
		selected = index;
	}

	private static boolean allowedNow() {
		Hero hero = Dungeon.hero;
		if (!isOn() || hero == null || !hero.isAlive() || !hero.ready) return false;
		return duringBosses() || Dungeon.level == null || !Dungeon.level.locked;
	}

	private static void restore(int target) {
		Snapshot snapshot = history.get(target);
		try {
			File root = SaveLocation.currentRoot();
			if (root == null) return;
			write(new File(root, GamesInProgress.gameFile(GamesInProgress.curSlot)), snapshot.game);
			write(new File(root, snapshot.levelFile), snapshot.level);
			index = target;
			selected = target;
			skipNext = true;
			GamesInProgress.setUnknown(GamesInProgress.curSlot);
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		} catch (Throwable e) {
			Log.e(TAG, "Could not restore a snapshot", e);
		}
	}

	static int count() {
		return history.size();
	}

	static int current() {
		return index;
	}

	static String describe(int position) {
		Snapshot snapshot = history.get(position);
		return "Depth " + snapshot.depth + ", turn " + snapshot.turn + ", HP " + snapshot.hp + "/" + snapshot.ht;
	}

	static int selected() {
		return selected;
	}

	/**
	 * Starts choosing from the step the game is on now.
	 */
	static void resetSelection() {
		selected = index;
	}

	static boolean canSelect(int position) {
		return position >= 0 && position < history.size() && allowedNow();
	}

	static void select(int position) {
		if (canSelect(position)) selected = position;
	}

	static boolean canSelectPrevious() {
		return canSelect(selected - 1);
	}

	static boolean canSelectNext() {
		return selected >= 0 && canSelect(selected + 1);
	}

	static void selectPrevious() {
		select(selected - 1);
	}

	static void selectNext() {
		select(selected + 1);
	}

	/**
	 * Whether the chosen step is a different one from where the game is now.
	 */
	static boolean selectionPending() {
		return selected >= 0 && selected != index && selected < history.size();
	}

	/**
	 * Loads the chosen step, if it isn't the one the game is on.
	 */
	static void loadSelected() {
		if (selectionPending() && allowedNow()) restore(selected);
	}

	/**
	 * A short label for the History list: the step's number, depth, turn and first log line.
	 */
	static String listLabel(int position) {
		Snapshot snapshot = history.get(position);
		String label = (position + 1) + ". D" + snapshot.depth + " T" + snapshot.turn;
		String line = snapshot.scene == null ? null : snapshot.scene.firstLine();
		if (line == null) return label + " HP " + snapshot.hp + "/" + snapshot.ht;
		if (line.length() > LIST_LINE_LENGTH) line = line.substring(0, LIST_LINE_LENGTH - 1) + "..";
		return label + " - " + line;
	}

	static UndoScene scene(int position) {
		if (position < 0 || position >= history.size()) return null;
		return history.get(position).scene;
	}

	/**
	 * Depth, turn, HP and max HP of a step, or null if there is no such step.
	 */
	static int[] stats(int position) {
		if (position < 0 || position >= history.size()) return null;
		Snapshot snapshot = history.get(position);
		return new int[] { snapshot.depth, snapshot.turn, snapshot.hp, snapshot.ht };
	}

	private static final String DEATH_BACKUP = "undo-death-backup";
	private static boolean deathBackup;

	/**
	 * Called at the start of Dungeon.deleteGame, which removes the floors of a game that has ended. When the hero
	 * has just died, the game's folder is copied first so Undo can bring it back.
	 */
	public static void beforeDeleteGame(int slot) {
		deathBackup = false;
		Hero hero = Dungeon.hero;
		if (!isOn() || hero == null || hero.isAlive() || index < 0 || slot != GamesInProgress.curSlot) return;
		try {
			File root = SaveLocation.currentRoot();
			File cache = SaveLocation.cacheDir();
			if (root == null || cache == null) return;
			File backup = new File(cache, DEATH_BACKUP);
			deleteRecursively(backup);
			copyFolder(new File(root, GamesInProgress.gameFolder(slot)), backup);
			deathBackup = true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not back up the game before it was deleted", e);
		}
	}

	static boolean canUndoDeath() {
		if (!deathBackup || index < 0 || index >= history.size()) return false;
		return duringBosses() || Dungeon.level == null || !Dungeon.level.locked;
	}

	/**
	 * Brings the game back to the moment before the action that killed the hero.
	 */
	static void undoDeath() {
		if (!canUndoDeath()) return;
		try {
			File root = SaveLocation.currentRoot();
			File cache = SaveLocation.cacheDir();
			if (root == null || cache == null) return;
			File backup = new File(cache, DEATH_BACKUP);
			File folder = new File(root, GamesInProgress.gameFolder(GamesInProgress.curSlot));
			copyFolder(backup, folder);
			deleteRecursively(backup);
			deathBackup = false;
			restore(index);
		} catch (Throwable e) {
			Log.e(TAG, "Could not undo the death", e);
		}
	}

	/**
	 * Called at the end of GameScene.gameOver. Adds an Undo button below the game's own buttons.
	 */
	public static void afterGameOver() {
		if (!canUndoDeath()) return;
		try {
			UndoTags.addDeathButton();
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the Undo button", e);
		}
	}

	private static void copyFolder(File source, File target) throws IOException {
		if (!target.isDirectory() && !target.mkdirs()) throw new IOException("Could not create " + target);
		File[] children = source.listFiles();
		if (children == null) return;
		for (File child : children) {
			File destination = new File(target, child.getName());
			if (child.isDirectory()) {
				copyFolder(child, destination);
				continue;
			}
			write(destination, read(child));
		}
	}

	private static void deleteRecursively(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) deleteRecursively(child);
		}
		file.delete();
	}

	static void clear() {
		history.clear();
		index = -1;
		selected = -1;
		skipNext = false;
	}

	private static void recordTiming(long saveNanos, long copyNanos) {
		lastSaveNanos = saveNanos;
		lastCopyNanos = copyNanos;
		long total = saveNanos + copyNanos;
		slowestNanos = Math.max(slowestNanos, total);
		totalNanos += total;
		timedSnapshots++;
	}

	static void resetTiming() {
		lastSaveNanos = lastCopyNanos = slowestNanos = totalNanos = 0;
		timedSnapshots = 0;
	}

	static String lastTiming() {
		if (timedSnapshots == 0) return "None yet";
		return ms(lastSaveNanos + lastCopyNanos) + " (save " + ms(lastSaveNanos) + ", copy " + ms(lastCopyNanos) + ")";
	}

	static String averageTiming() {
		return timedSnapshots == 0 ? "None yet" : ms(totalNanos / timedSnapshots) + " over " + timedSnapshots;
	}

	static String slowestTiming() {
		return timedSnapshots == 0 ? "None yet" : ms(slowestNanos);
	}

	static String memoryText() {
		long bytes = 0;
		for (Snapshot snapshot : history) bytes += snapshot.size();
		return history.size() + " kept, " + Math.round(bytes / 1024f) + " KB";
	}

	private static String ms(long nanos) {
		return Math.round(nanos / 100_000f) / 10f + " ms";
	}

	private static byte[] read(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			int offset = 0;
			while (offset < data.length) {
				int read = in.read(data, offset, data.length - offset);
				if (read < 0) break;
				offset += read;
			}
			return data;
		}
	}

	private static void write(File file, byte[] data) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			out.write(data);
		}
	}
}
